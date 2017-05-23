package io.rapid;


import android.os.Handler;


/**
 * Rapid.io document reference
 * <p>
 * Provides methods for querying data and mutating data
 *
 * @param <T> type of the items in collection
 */
@SuppressWarnings("WeakerAccess")
public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;
	private final RapidDocumentSubscription<T> mSubscription;
	private Handler mUiThreadHandler;


	public interface DocumentTransformer<T> {
		T getMutatedDocument(RapidDocument<T> oldDocument);
	}


	public interface DocumentDeleteTransformer<T> {
		boolean shouldDeleteDocument(RapidDocument<T> oldDocument);
	}


	RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl) {
		this(uiThreadHandler, collectionName, impl, IdProvider.getNewDocumentId());
	}


	RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl, String documentId) {
		mUiThreadHandler = uiThreadHandler;
		mId = documentId;
		mImpl = impl;
		mSubscription = new RapidDocumentSubscription<>(documentId, collectionName, uiThreadHandler);
	}


	/**
	 * Returns unique document ID
	 *
	 * @return document ID
	 */
	public String getId() {
		return mId;
	}


	/**
	 * Mutate document (set new value)
	 *
	 * @param item new content for the document
	 * @return RapidFuture providing callbacks for onComplete, onError, onSuccess events
	 */
	public RapidFuture mutate(T item) {
		return mutate(item, null);
	}


	/**
	 * Mutate document (set new value)
	 * <p>
	 * Operation will fail when provided etag value does not equal current etag on backend (document was modified in the meantime)
	 *
	 * @param item new content for the document
	 * @param etag etag value expected to be present on backend
	 * @return RapidFuture providing callbacks for onComplete, onError, onSuccess events
	 */
	public RapidFuture mutate(T item, String etag) {
		return mImpl.mutate(mId, item, etag);
	}


	/**
	 * Concurrency-safe mutate document (set new value)
	 * <p>
	 * When you provide document transformer, this operation will safely update document value,
	 * because within documentTransformer you can be sure you are updating the latest version available on backend.
	 * <p>
	 * The SDK will automatically try to update the document until there is no conflicting version on backend so the document
	 * transformer function may be called multiple times.
	 *
	 * @param documentTransformer function responsible for updating the latest version of document available on backend
	 * @return RapidFuture providing callbacks for onComplete, onError, onSuccess events
	 */
	public RapidFuture concurrencySafeMutate(DocumentTransformer<T> documentTransformer) {
		RapidFuture result = new RapidFuture(mUiThreadHandler);
		fetch(document -> {
					T updated = documentTransformer.getMutatedDocument(document);
					mutate(updated, document.getEtag()).onError(error -> {
						if(error.getType() == RapidError.ErrorType.ETAG_CONFLICT) {
							concurrencySafeMutate(documentTransformer)
									.onSuccess(result::invokeSuccess)
									.onError(result::invokeError);
						} else {
							result.invokeError(error);
						}
					}).onSuccess(result::invokeSuccess);
				}
		);
		return result;
	}


	/**
	 * Delete the document from collection
	 *
	 * @return RapidFuture providing callbacks for onComplete, onError, onSuccess events
	 */
	public RapidFuture delete() {
		return delete(null);
	}


	/**
	 * Delete the document from collection
	 * <p>
	 * Operation will fail when provided etag value does not equal current etag on backend (document was modified in the meantime)
	 *
	 * @param etag etag value expected to be present on backend
	 * @return
	 */
	public RapidFuture delete(String etag) {
		return mImpl.mutate(mId, null, etag);
	}


	/**
	 * Concurrency-safe mutate document (set new value)
	 * <p>
	 * When you provide document transformer, this operation will safely delete document value,
	 * because within documentTransformer you can be sure you are updating the latest version available on backend.
	 * <p>
	 * The SDK will automatically try to delete the document until there is no conflicting version on backend so the document
	 * transformer function may be called multiple times.
	 *
	 * @param documentDeleteTransformer function responsible for deleting the latest version of document available on backend - return true if document should be deleted
	 * @return RapidFuture providing callbacks for onComplete, onError, onSuccess events
	 */
	public RapidFuture safeDelete(DocumentDeleteTransformer<T> documentDeleteTransformer) {
		return concurrencySafeMutate(oldDocument -> documentDeleteTransformer.shouldDeleteDocument(oldDocument) ? null : oldDocument.getBody());
	}


	/**
	 * Subscribe to single document updates
	 *
	 * @param callback callback function providing updated document on Main thread
	 */
	public void subscribe(RapidCallback.Document<T> callback) {
		mSubscription.setCallback(callback);
		mImpl.subscribe(mSubscription);
	}


	/**
	 * One-time fetch document value
	 *
	 * @param callback callback function providing document value on Main thread
	 */
	public void fetch(RapidCallback.Document<T> callback) {
		mSubscription.setCallback(callback);
		mImpl.fetch(mSubscription);
	}
}

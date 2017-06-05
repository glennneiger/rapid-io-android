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
	public RapidFuture mutate(T item, Etag etag) {
		return mImpl.mutate(mId, item, etag);
	}


	/**
	 * Concurrency-safe mutate/delete document (set new value)
	 * <p>
	 * When you provide document transformer, this operation will safely update document value,
	 * because within documentExecutor you can be sure you are updating the latest version available on backend.
	 * <p>
	 * The SDK will automatically try to update the document until there is no conflicting version on backend so the document
	 * transformer function may be called multiple times.
	 *
	 * @param documentExecutor function responsible for updating the latest version of document available on backend
	 *                         you need to return one of `RapidDocumentExecutor.mutate(value)`, `RapidDocumentExecutor.delete()` or `RapidDocumentExecutor.cancel()`
	 * @return RapidFuture providing callbacks for onComplete, onError, onSuccess events
	 */
	public RapidFuture execute(RapidDocumentExecutor.Callback<T> documentExecutor) {
		RapidFuture result = new RapidFuture(mUiThreadHandler);
		fetch(document -> {
					RapidDocumentExecutor.Result<T> executorResult = documentExecutor.execute(document);
					if(executorResult.getType() == RapidDocumentExecutor.Result.TYPE_CANCEL) {
						result.invokeSuccess();
					} else if(executorResult.getType() == RapidDocumentExecutor.Result.TYPE_MUTATE) {
						mutate(executorResult.getValue(), document != null ? document.getEtag() : Etag.NO_ETAG).onError(error -> {
							if(error.getType() == RapidError.ErrorType.ETAG_CONFLICT) {
								execute(documentExecutor)
										.onSuccess(result::invokeSuccess)
										.onError(result::invokeError);
							} else {
								result.invokeError(error);
							}
						}).onSuccess(result::invokeSuccess);
					}
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
	public RapidFuture delete(Etag etag) {
		return mImpl.mutate(mId, null, etag);
	}


	/**
	 * Subscribe to single document updates
	 *
	 * @param callback callback function providing updated document on Main thread
	 */
	public RapidDocumentSubscription<T> subscribe(RapidCallback.Document<T> callback) {
		if (mSubscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		mSubscription.setCallback(callback);
		mImpl.subscribe(mSubscription);
		return mSubscription;
	}


	/**
	 * One-time fetch document value
	 *
	 * @param callback callback function providing document value on Main thread
	 */
	public RapidDocumentSubscription<T> fetch(RapidCallback.Document<T> callback) {
		if (mSubscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		mSubscription.setCallback(callback);
		mImpl.fetch(mSubscription);
		return mSubscription;
	}
}

package io.rapid;


import android.os.Handler;


public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;
	private final RapidDocumentSubscription<T> mSubscription;
	private Handler mUiThreadHandler;


	public interface DocumentTransformator<T> {
		T getMutatedDocument(RapidDocument<T> oldDocument);
	}


	public interface DocumentDeleteTransformator<T> {
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


	public String getId() {
		return mId;
	}


	public RapidFuture mutate(T item) {
		return mutate(item, null);
	}


	public RapidFuture mutate(T item, String etag) {
		return mImpl.mutate(mId, item, etag);
	}


	public RapidFuture safeMutate(DocumentTransformator<T> documentTransformator) {
		RapidFuture result = new RapidFuture(mUiThreadHandler);
		fetch(document -> {
					T updated = documentTransformator.getMutatedDocument(document);
					mutate(updated, document.getEtag()).onError(error -> {
						if(error.getType() == RapidError.ErrorType.ETAG_CONFLICT) {
							safeMutate(documentTransformator)
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


	public RapidFuture delete() {
		return delete(null);
	}


	public RapidFuture delete(String etag) {
		return mImpl.mutate(mId, null, etag);
	}


	public RapidFuture safeDelete(DocumentDeleteTransformator<T> documentDeleteTransformator) {
		return safeMutate(oldDocument -> documentDeleteTransformator.shouldDeleteDocument(oldDocument) ? null : oldDocument.getBody());
	}


	public void subscribe(RapidCallback.Document<T> callback) {
		mSubscription.setCallback(callback);
		mImpl.subscribe(mSubscription);
	}


	public void fetch(RapidCallback.Document<T> callback) {
		mSubscription.setCallback(callback);
		mImpl.fetch(mSubscription);
	}
}

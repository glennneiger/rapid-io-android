package io.rapid;


import android.os.Handler;


public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;
	private final RapidDocumentSubscription<T> mSubscription;


	RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl) {
		this(uiThreadHandler, collectionName, impl, IdProvider.getNewDocumentId());
	}


	RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl, String documentId) {
		mId = documentId;
		mImpl = impl;
		mSubscription = new RapidDocumentSubscription<>(documentId, collectionName, uiThreadHandler);
	}


	public String getId() {
		return mId;
	}


	public RapidFuture mutate(T item) {
		return mImpl.mutate(mId, item);
	}


	public RapidFuture delete() {
		return mImpl.mutate(mId, null);
	}


	public void subscribe(RapidCallback.Document<T> callback) {
		mSubscription.setCallback(callback);
		mImpl.subscribe(mSubscription);
	}
}

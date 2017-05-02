package io.rapid;


import android.os.Handler;


public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;
	private final RapidDocumentSubscription<T> mSubscription;


	public RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl) {
		this(uiThreadHandler, collectionName, impl, IdProvider.getNewDocumentId());
	}


	public RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl, String documentId) {
		mId = documentId;
		mImpl = impl;
		Handler uiThreadHandler1 = uiThreadHandler;
		mSubscription = new RapidDocumentSubscription<>(documentId, collectionName, uiThreadHandler1);
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

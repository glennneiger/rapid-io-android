package io.rapid;


import android.os.Handler;


public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;
	private final RapidDocumentSubscription<T> mSubscription;
	private final Handler mUiThreadHandler;


	public RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl) {
		this(uiThreadHandler, collectionName, impl, IdProvider.getNewDocumentId());
	}


	public RapidDocumentReference(Handler uiThreadHandler, String collectionName, CollectionConnection<T> impl, String documentId) {
		mId = documentId;
		mImpl = impl;
		mUiThreadHandler = uiThreadHandler;
		mSubscription = new RapidDocumentSubscription<T>(collectionName, documentId, mUiThreadHandler);
	}


	public String getId() {
		return mId;
	}


	public RapidFuture<T> mutate(T item) {
		return mImpl.mutate(mId, item);
	}


	public void subscribe(RapidDocumentCallback<T> callback) {
		mSubscription.setCallback(callback);
		mImpl.subscribeDocument(mSubscription);
	}
}

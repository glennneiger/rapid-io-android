package io.rapid;


public class RapidDocument<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;


	public RapidDocument(String collectionName, CollectionConnection<T> impl) {
		this(collectionName, impl, IdProvider.getNewDocumentId());
	}


	public RapidDocument(String collectionName, CollectionConnection<T> impl, String documentId) {
		mId = documentId;
		mImpl = impl;
	}


	public String getId() {
		return mId;
	}

	public RapidFuture<T> mutate(T item) {
		return mImpl.set(mId, item);
	}


	public RapidSubscription subscribe(RapidDocumentCallback<T> callback) {
		return mImpl.subscribeDocument(callback);
	}
}

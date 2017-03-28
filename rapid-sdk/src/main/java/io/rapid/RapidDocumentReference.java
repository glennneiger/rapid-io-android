package io.rapid;


public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;


	public RapidDocumentReference(String collectionName, CollectionConnection<T> impl) {
		this(collectionName, impl, IdProvider.getNewDocumentId());
	}


	public RapidDocumentReference(String collectionName, CollectionConnection<T> impl, String documentId) {
		mId = documentId;
		mImpl = impl;
	}


	public String getId() {
		return mId;
	}

	public RapidFuture<T> mutate(T item) {
		return mImpl.mutate(mId, item);
	}


	public RapidDocumentSubscription<T> subscribe(RapidDocumentCallback<T> callback) {
		return mImpl.subscribeDocument(mId, callback);
	}
}

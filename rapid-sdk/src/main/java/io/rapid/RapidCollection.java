package io.rapid;


import java.util.Collection;


public class RapidCollection<T> {

	private final String mCollectionName;
	private RapidQuery<T> mQuery;
	private RapidCollectionImpl<T> mImpl;


	public RapidCollection(Rapid rapid, String collectionName, Class<T> type) {
		mCollectionName = collectionName;
		mQuery = new RapidQuery<>(mCollectionName);
		mImpl = new MockRapidCollectionImpl<>(type, rapid.getJsonConverter());
	}


	public RapidFuture<T> add(T item) {
		return mImpl.add(item);
	}


	public RapidFuture<T> edit(String key, T item) {
		return mImpl.edit(key, item);
	}


	public RapidQuery<T> query() {
		return mQuery;
	}


	public RapidSubscription subscribe(RapidObjectCallback<Collection<T>> callback) {
		return mImpl.subscribe(callback);
	}
}

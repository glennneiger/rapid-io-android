package io.rapid;


import java.util.Collection;


public class RapidCollection<T> {

	private final Rapid mRapid;
	private final String mCollectionName;
	private RapidQuery<T> mQuery;


	public RapidCollection(Rapid rapid, String collectionName) {
		mRapid = rapid;
		mCollectionName = collectionName;
		mQuery = new RapidQuery<>(mCollectionName);
	}


	public RapidFuture<T> mutate(T item) {
		return new RapidFuture<>();
	}


	public RapidQuery<T> query() {
		return mQuery;
	}


	public RapidSubscription subscribe(RapidObjectCallback<Collection<T>> callback) {
		return mQuery.subscribe(callback);
	}
}

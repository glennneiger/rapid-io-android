package io.rapid;


import java.util.Collection;


public class RapidCollection<T> {

	private final Rapid mRapid;
	private RapidQuery<T> mQuery;


	public RapidCollection(Rapid rapid, String collectionName) {
		mRapid = rapid;
		mQuery = new RapidQuery<>(collectionName);
	}


	public RapidFuture<T> mutate(T item) {
		return new RapidFuture<>();
	}


	public RapidQuery<T> query() {
		return mQuery;
	}


	public RapidSubscription<Collection<T>> subscribe(RapidObjectCallback<Collection<T>> callback) {
		return mQuery.subscribe(callback);
	}
}

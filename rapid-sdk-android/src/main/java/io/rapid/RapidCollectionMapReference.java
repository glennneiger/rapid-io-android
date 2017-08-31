package io.rapid;


import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class RapidCollectionMapReference<T, S> {

	private final MapFunction<T, S> mMapFunction;
	private final RapidCollectionReference<T> mCollectionReference;
	private AuthHelper mAuthHelper;


	public interface MapFunction<T, S> {
		@NonNull
		S map(RapidDocument<T> document);
	}


	public interface MapInnerFunction<T, S> {
		@NonNull
		S map(T item);
	}


	public interface MapCollectionUpdatesCallback<T> {
		void onValueChanged(List<T> items, ListUpdate listUpdate);
	}


	RapidCollectionMapReference(RapidCollectionReference<T> collectionReference, MapFunction<T, S> mapFunction, AuthHelper authHelper) {
		mCollectionReference = collectionReference;
		mMapFunction = mapFunction;
		mAuthHelper = authHelper;
	}


	public RapidCollectionSubscription<T> subscribe(@NonNull RapidCallback.CollectionMapped<S> callback) {
		return subscribeWithListUpdates((items, listUpdate) -> callback.onValueChanged(items));
	}


	public RapidCollectionSubscription<T> subscribeWithListUpdates(@NonNull MapCollectionUpdatesCallback<S> callback) {
		RapidCollectionSubscription<T> subscription = mCollectionReference.getSubscription();
		subscription.setCallback((rapidDocuments, listUpdate) -> {
			List<S> result = new ArrayList<>();
			for(RapidDocument<T> rapidDocument : rapidDocuments) {
				result.add(mMapFunction.map(rapidDocument));
			}
			callback.onValueChanged(result, listUpdate);
		});
		subscription.setAuthToken(mAuthHelper.getAuthToken());
		mCollectionReference.getConnection().subscribe(subscription);

		return subscription;
	}


	public RapidCollectionSubscription<T> fetch(@NonNull RapidCallback.CollectionMapped<S> callback) {
		RapidCollectionSubscription<T> subscription = mCollectionReference.getSubscription();
		if(subscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		subscription.setCallback((rapidDocuments, listUpdate) -> {
			List<S> result = new ArrayList<>();
			for(RapidDocument<T> rapidDocument : rapidDocuments) {
				result.add(mMapFunction.map(rapidDocument));
			}
			callback.onValueChanged(result);
		});
		mCollectionReference.getConnection().fetch(subscription);

		return subscription;
	}


	@NonNull
	public <U> RapidCollectionMapReference<T, U> map(@NonNull MapInnerFunction<S, U> mapFunction, AuthHelper auth) {
		return new RapidCollectionMapReference<>(mCollectionReference, document -> mapFunction.map(mMapFunction.map(document)), auth);
	}
}

package io.rapid;


import android.support.annotation.NonNull;

import java.util.List;


public class RapidDocumentMapReference<T, S> {

	private final MapFunction<T, S> mMapFunction;
	private final RapidDocumentReference<T> mDocumentReference;
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


	RapidDocumentMapReference(RapidDocumentReference<T> documentReference, MapFunction<T, S> mapFunction, AuthHelper authHelper) {
		mDocumentReference = documentReference;
		mMapFunction = mapFunction;
		mAuthHelper = authHelper;
	}


	public RapidDocumentSubscription<T> subscribe(@NonNull RapidCallback.DocumentMapped<S> callback) {
		RapidDocumentSubscription<T> subscription = mDocumentReference.getSubscription();
		if(subscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		subscription.setCallback(document -> {
			callback.onValueChanged(mMapFunction.map(document));
		});
		subscription.setAuthToken(mAuthHelper.getAuthToken());
		mDocumentReference.getConnection().subscribe(subscription);
		return subscription;
	}


	public RapidDocumentSubscription<T> fetch(@NonNull RapidCallback.DocumentMapped<S> callback) {
		RapidDocumentSubscription<T> subscription = mDocumentReference.getSubscription();
		if(subscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		subscription.setCallback(document -> {
			callback.onValueChanged(mMapFunction.map(document));
		});
		mDocumentReference.getConnection().fetch(subscription);
		return subscription;
	}


	@NonNull
	public <U> RapidDocumentMapReference<T, U> map(@NonNull MapInnerFunction<S, U> mapFunction, AuthHelper auth) {
		return new RapidDocumentMapReference<>(mDocumentReference, document -> mapFunction.map(mMapFunction.map(document)), auth);
	}
}

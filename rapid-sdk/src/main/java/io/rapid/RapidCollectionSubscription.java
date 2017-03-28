package io.rapid;


import java.util.List;

public class RapidCollectionSubscription<T> extends Subscription {

	private final RapidCollectionCallback<T> mCallback;


	RapidCollectionSubscription(RapidCollectionCallback<T> callback) {
		mCallback = callback;
	}


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}


	void invokeChange(List<RapidDocument<T>> value) {
		mCallback.onValueChanged(value);
	}
}
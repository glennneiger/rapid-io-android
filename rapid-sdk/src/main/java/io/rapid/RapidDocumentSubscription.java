package io.rapid;


public class RapidDocumentSubscription<T> extends Subscription {

	private final RapidDocumentCallback<T> mCallback;


	RapidDocumentSubscription(RapidDocumentCallback<T> callback) {
		mCallback = callback;
	}


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}


	void invokeChange(RapidDocument<T> value) {
		mCallback.onValueChanged(value);
	}
}

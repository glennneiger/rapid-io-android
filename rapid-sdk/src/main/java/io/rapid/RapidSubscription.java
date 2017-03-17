package io.rapid;


import java.util.Collection;


public class RapidSubscription<T> {

	private final RapidCollectionCallback<T> mCallback;
	private boolean mSubscribed = true;
	private OnUnsubscribeCallback mOnUnsubscribeCallback;


	interface OnUnsubscribeCallback {
		void onUnsubscribe();
	}


	public RapidSubscription(RapidCollectionCallback<T> callback) {
		mCallback = callback;
	}


	public RapidSubscription onError(io.rapid.ErrorCallback callback) {
		return this;
	}


	public void unsubscribe() {
		mSubscribed = false;
		if(mOnUnsubscribeCallback != null)
			mOnUnsubscribeCallback.onUnsubscribe();
	}


	public boolean isSubscribed() {
		return mSubscribed;
	}


	public void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}


	public void invokeChange(Collection<RapidWrapper<T>> value) {
		mCallback.onValueChanged(value);
	}
}

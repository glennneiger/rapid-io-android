package io.rapid;


import android.os.Handler;


abstract class BaseSubscription {
	final Handler mUiThreadHandler;
	RapidCallback.Error mErrorCallback;
	private Subscription.OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed;
	private String mSubscriptionId;


	BaseSubscription(Handler uiThreadHandler) {mUiThreadHandler = uiThreadHandler;}


	public BaseSubscription onError(RapidCallback.Error callback) {
		mErrorCallback = callback;
		return this;
	}


	public void unsubscribe() {
		if(mSubscribed) {
			mSubscribed = false;
			if(mOnUnsubscribeCallback != null)
				mOnUnsubscribeCallback.onUnsubscribe();
		}
	}


	public String getSubscriptionId() {
		return mSubscriptionId;
	}


	public void setSubscriptionId(String subscriptionId) {
		mSubscriptionId = subscriptionId;
	}


	public boolean isSubscribed() {
		return mSubscribed;
	}


	public void setSubscribed(boolean subscribed) {
		mSubscribed = subscribed;
	}


	synchronized void invokeError(RapidError error) {
		if(mErrorCallback != null && mSubscribed) {
			mSubscribed = false;

			mUiThreadHandler.post(() -> {
				synchronized(mErrorCallback) {
					mErrorCallback.onError(error);
				}
			});
		}
	}


	void setOnUnsubscribeCallback(Subscription.OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}
}

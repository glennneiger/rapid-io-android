package io.rapid;


import android.os.Handler;


abstract class Subscription {
	final Handler mUiThreadHandler;
	private RapidCallback.Error mErrorCallback;
	private BaseCollectionSubscription.OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed;
	private String mSubscriptionId;


	Subscription(Handler uiThreadHandler) {mUiThreadHandler = uiThreadHandler;}


	public Subscription onError(RapidCallback.Error callback) {
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


	void setSubscribed(boolean subscribed) {
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


	void setOnUnsubscribeCallback(BaseCollectionSubscription.OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}
}

package io.rapid;


import io.rapid.executor.RapidExecutor;


abstract class Subscription {
	final RapidExecutor mExecutor;
	private RapidCallback.Error mErrorCallback;
	private BaseCollectionSubscription.OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed;
	private String mSubscriptionId;
	private String mAuthToken;


	Subscription(RapidExecutor executor) {mExecutor = executor;}


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


	String getAuthToken() {return mAuthToken;}


	void setAuthToken(String authToken) {mAuthToken = authToken;}


	synchronized void invokeError(RapidError error) {
		if(mErrorCallback != null && mSubscribed) {
			mSubscribed = false;

			mExecutor.doOnMain(() -> {
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

package io.rapid;


import io.rapid.executor.RapidExecutor;


abstract class Subscription {
	final RapidExecutor mExecutor;
	private RapidCallback.Error mErrorCallback;
	private BaseCollectionSubscription.OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed;
	private String mSubscriptionId;


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


	synchronized void invokeError(final RapidError error) {
		if(mErrorCallback != null && mSubscribed) {
			mSubscribed = false;

			mExecutor.doOnMain(new Runnable() {
				@Override
				public void run() {
					synchronized(mErrorCallback) {
						mErrorCallback.onError(error);
					}
				}
			});
		}
	}


	void setOnUnsubscribeCallback(BaseCollectionSubscription.OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}
}

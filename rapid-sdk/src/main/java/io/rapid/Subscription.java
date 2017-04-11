package io.rapid;


import android.os.Handler;


abstract class Subscription<T> {
	final Handler mUiThreadHandler;
	final String mCollectionName;
	OnUnsubscribeCallback mOnUnsubscribeCallback;
	private String mSubscriptionId;
	boolean mSubscribed = true;
	RapidCallback.Error mErrorCallback;


	interface OnUnsubscribeCallback {
		void onUnsubscribe();
	}


	Subscription(String collectionName, Handler uiThreadHandler) {
		mUiThreadHandler = uiThreadHandler;
		mCollectionName = collectionName;
	}


	abstract void onDocumentUpdated(String previousSiblingId, RapidDocument<T> document);


	abstract int getSkip();


	abstract int getLimit();


	abstract Filter getFilter();


	abstract EntityOrder getOrder();


	public abstract Subscription onError(RapidCallback.Error callback);


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


	public String getCollectionName() {
		return mCollectionName;
	}


	synchronized void invokeError(RapidError error) {
		if(mErrorCallback != null && mSubscribed)
		{
			mSubscribed = false;

			mUiThreadHandler.post(() -> {
				synchronized(mErrorCallback){
					mErrorCallback.onError(error);
				}
			});
		}
	}
}

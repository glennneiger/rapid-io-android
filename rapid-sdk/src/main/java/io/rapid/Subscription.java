package io.rapid;


import android.os.Handler;


abstract class Subscription<T> {
	final Handler mUiThreadHandler;
	final String mCollectionName;
	OnUnsubscribeCallback mOnUnsubscribeCallback;
	private String mSubscriptionId;
	private boolean mSubscribed = true;


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


	public void unsubscribe() {
		mSubscribed = false;
		if(mOnUnsubscribeCallback != null)
			mOnUnsubscribeCallback.onUnsubscribe();
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


	public Subscription onError(ErrorCallback callback) {
		return this;
	}


	public String getCollectionName() {
		return mCollectionName;
	}
}

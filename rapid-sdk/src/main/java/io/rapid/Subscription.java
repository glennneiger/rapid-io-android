package io.rapid;


import android.os.Handler;


abstract class Subscription<T> {
	final Handler mUiThreadHandler;
	final String mCollectionName;
	OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed = true;


	interface OnUnsubscribeCallback {
		void onUnsubscribe();
	}


	Subscription(String collectionName, Handler uiThreadHandler) {
		mUiThreadHandler = uiThreadHandler;
		mCollectionName = collectionName;
	}


	abstract void onDocumentUpdated(RapidDocument<T> document);


	abstract MessageSub createSubscriptionMessage(String subscriptionId);


	public void unsubscribe() {
		mSubscribed = false;
		if(mOnUnsubscribeCallback != null)
			mOnUnsubscribeCallback.onUnsubscribe();
	}


	public boolean isSubscribed() {
		return mSubscribed;
	}


	public Subscription onError(ErrorCallback callback) {
		return this;
	}
}

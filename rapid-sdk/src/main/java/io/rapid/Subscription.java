package io.rapid;


import android.os.Handler;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


abstract class Subscription<T> {
	final Handler mUiThreadHandler;
	final String mCollectionName;
	OnUnsubscribeCallback mOnUnsubscribeCallback;
	boolean mSubscribed = true;
	RapidCallback.Error mErrorCallback;
	private String mSubscriptionId;


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


	public String getFingerprint() throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		long startMs = System.currentTimeMillis();
		StringBuilder subscriptionString = new StringBuilder();
		subscriptionString.append(getCollectionName());
		subscriptionString.append("#");
		subscriptionString.append(getFilter().toJson());
		subscriptionString.append("#");
		subscriptionString.append(getLimit());
		subscriptionString.append("#");
		subscriptionString.append(getOrder().toJson());
		subscriptionString.append("#");
		subscriptionString.append(getSkip());
		String input = subscriptionString.toString();
		String hash = Sha1Utility.sha1(input);
		Logcat.i("SHA1: %s : %s; Took %dms", input, hash, System.currentTimeMillis() - startMs);
		return hash;
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


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}
}

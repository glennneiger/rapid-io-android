package io.rapid;


import android.os.Handler;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.rapid.utility.Sha1Utility;


public abstract class Subscription<T> {
	final Handler mUiThreadHandler;
	private final String mCollectionName;
	private OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed;
	RapidCallback.Error mErrorCallback;
	private String mSubscriptionId;
	private String mFingerprintCache;
	protected DataState mDataState = DataState.NO_DATA;


	public enum DataState {NO_DATA, LOADED_FROM_DISK_CACHE, LOADED_FROM_MEMORY_CACHE, LOADED_FROM_SERVER}


	interface OnUnsubscribeCallback {
		void onUnsubscribe();
	}


	Subscription(String collectionName, Handler uiThreadHandler) {
		mUiThreadHandler = uiThreadHandler;
		mCollectionName = collectionName;
	}


	abstract int onDocumentUpdated(RapidDocument<T> document);


	abstract int getSkip();


	abstract int getLimit();


	abstract Filter getFilter();


	abstract EntityOrder getOrder();


	abstract List<RapidDocument<T>> getDocuments();


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


	public void setSubscribed(boolean subscribed) {
		mSubscribed = subscribed;
	}


	public DataState getDataState() {
		return mDataState;
	}


	String getFingerprint() throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		if(mFingerprintCache == null) {
			long startMs = System.currentTimeMillis();
			StringBuilder subscriptionString = new StringBuilder();
			subscriptionString.append(getCollectionName());
			subscriptionString.append("#");
			if(getFilter() !=  null) subscriptionString.append(getFilter().toJson());
			subscriptionString.append("#");
			subscriptionString.append(getLimit());
			subscriptionString.append("#");
			if(getOrder() != null) subscriptionString.append(getOrder().toJson());
			subscriptionString.append("#");
			subscriptionString.append(getSkip());
			String input = subscriptionString.toString();
			String hash = Sha1Utility.sha1(input);
			Logcat.i("Subscription hash calculation: %s : %s; Took %dms", input, hash, System.currentTimeMillis() - startMs);
			mFingerprintCache = hash;
		}
		return mFingerprintCache;
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


	void invalidateFingerprintCache() {
		mFingerprintCache = null;
	}


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}
}

package io.rapid;


import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.rapid.utility.Sha1Utility;


public abstract class BaseCollectionSubscription<T> extends Subscription {
	private final String mCollectionName;
	private String mFingerprintCache;
	protected DataState mDataState = DataState.NO_DATA;


	public enum DataState {NO_DATA, LOADED_FROM_DISK_CACHE, LOADED_FROM_MEMORY_CACHE, LOADED_FROM_SERVER}


	interface OnUnsubscribeCallback {
		void onUnsubscribe();
	}


	BaseCollectionSubscription(String collectionName, RapidExecutor executor) {
		super(executor);
		mCollectionName = collectionName;
	}


	abstract int onDocumentUpdated(RapidDocument<T> document);


	abstract int getSkip();


	abstract int getLimit();


	abstract Filter getFilter();


	abstract EntityOrder getOrder();


	abstract List<RapidDocument<T>> getDocuments();


	public String getCollectionName() {
		return mCollectionName;
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
			Logcat.i("BaseCollectionSubscription hash calculation: %s : %s; Took %dms", input, hash, System.currentTimeMillis() - startMs);
			mFingerprintCache = hash;
		}
		return mFingerprintCache;
	}


	void invalidateFingerprintCache() {
		mFingerprintCache = null;
	}


}

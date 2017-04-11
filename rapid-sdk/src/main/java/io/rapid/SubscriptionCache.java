package io.rapid;


import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


public class SubscriptionCache {

	private static final int DEFAULT_INDEX = 0;
	private DiskLruCache mCache;


	public SubscriptionCache(Context context, int maxSizeInMb) throws IOException {
		// TODO better cache dir
		mCache = DiskLruCache.open(new File(context.getCacheDir() + "/rapid"), 0, 1, maxSizeInMb * 1_000_000);
	}


	public static String getSubscriptionFingerprint(RapidCollectionSubscription subscription) throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		StringBuilder subscriptionString = new StringBuilder();
		subscriptionString.append(subscription.getCollectionName());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getFilter().toJson());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getLimit());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getOrder());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getSkip());

		return Sha1Utility.sha1(subscriptionString.toString());
	}


	public String get(RapidCollectionSubscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
		String fingerprint = getSubscriptionFingerprint(subscription);
		return mCache.get(fingerprint).getString(DEFAULT_INDEX);
	}


	public void put(RapidCollectionSubscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
		String fingerprint = getSubscriptionFingerprint(subscription);
		DiskLruCache.Editor editor = mCache.edit(fingerprint);
		editor.set(DEFAULT_INDEX, jsonValue);
		editor.commit();
	}


	public void clear() throws IOException {
		mCache.delete();
	}
}

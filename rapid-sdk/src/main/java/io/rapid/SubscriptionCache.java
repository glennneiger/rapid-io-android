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


	public SubscriptionCache(Context context, String apiKey, int maxSizeInMb) throws IOException {
		// TODO better cache dir
		mCache = DiskLruCache.open(new File(context.getCacheDir() + "/rapid/" + apiKey), 0, 1, maxSizeInMb * 1_000_000);
	}


	public static String getSubscriptionFingerprint(RapidCollectionSubscription subscription) throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
		StringBuilder subscriptionString = new StringBuilder();
		subscriptionString.append(subscription.getCollectionName());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getFilter().toJson());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getLimit());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getOrder().toJson());
		subscriptionString.append("#");
		subscriptionString.append(subscription.getSkip());
		String input = subscriptionString.toString();
		String hash = Sha1Utility.sha1(input);
		Logcat.i("SHA1: %s : %s", input, hash);
		return hash;
	}


	public String get(RapidCollectionSubscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
		String fingerprint = getSubscriptionFingerprint(subscription);
		DiskLruCache.Snapshot record = mCache.get(fingerprint);
		if(record != null) {
			String jsonValue = record.getString(DEFAULT_INDEX);
			Logcat.d("Reading from subscription cache. key=%s; value=%s", fingerprint, jsonValue);
			return jsonValue;
		}
		Logcat.d("Reading from subscription cache. key=%s; value=null", fingerprint);
		return null;
	}


	public void put(RapidCollectionSubscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
		String fingerprint = getSubscriptionFingerprint(subscription);
		DiskLruCache.Editor editor = mCache.edit(fingerprint);
		editor.set(DEFAULT_INDEX, jsonValue);
		editor.commit();
		Logcat.d("Saving to subscription cache. key=%s; value=%s", fingerprint, jsonValue);
	}


	public void clear() throws IOException {
		mCache.delete();
	}


	public void remove(RapidCollectionSubscription subscription) throws IOException, NoSuchAlgorithmException, JSONException {
		String fingerprint = getSubscriptionFingerprint(subscription);
		mCache.remove(fingerprint);
		Logcat.d("Removing from subscription cache. key=%s", fingerprint);
	}
}

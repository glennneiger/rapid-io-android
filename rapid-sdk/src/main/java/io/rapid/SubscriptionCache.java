package io.rapid;


import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class SubscriptionCache {

	private static final int DEFAULT_INDEX = 0;
	private DiskLruCache mCache;


	public SubscriptionCache(Context context, String apiKey, int maxSizeInMb) throws IOException {
		// TODO better cache dir
		mCache = DiskLruCache.open(new File(context.getCacheDir() + "/rapid/" + apiKey), 0, 1, maxSizeInMb * 1_000_000);
	}




	public String get(Subscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
		String fingerprint = subscription.getFingerprint();
		DiskLruCache.Snapshot record = mCache.get(fingerprint);
		if(record != null) {
			String jsonValue = record.getString(DEFAULT_INDEX);
			Logcat.d("Reading from subscription cache. key=%s; value=%s", fingerprint, jsonValue);
			return jsonValue;
		}
		Logcat.d("Reading from subscription cache. key=%s; value=null", fingerprint);
		return null;
	}


	public void put(Subscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
		String fingerprint = subscription.getFingerprint();
		DiskLruCache.Editor editor = mCache.edit(fingerprint);
		editor.set(DEFAULT_INDEX, jsonValue);
		editor.commit();
		Logcat.d("Saving to subscription cache. key=%s; value=%s", fingerprint, jsonValue);
	}


	public void clear() throws IOException {
		mCache.delete();
	}


	public void remove(Subscription subscription) throws IOException, NoSuchAlgorithmException, JSONException {
		String fingerprint = subscription.getFingerprint();
		mCache.remove(fingerprint);
		Logcat.d("Removing from subscription cache. key=%s", fingerprint);
	}
}

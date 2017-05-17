package io.rapid;


import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import io.rapid.utility.Sha1Utility;


class SubscriptionDiskCache {

	private static final int DEFAULT_INDEX = 0;
	private DiskLruCache mCache;
	private boolean mEnabled = true;


	SubscriptionDiskCache(Context context, String apiKey, int maxSizeInMb) throws IOException {
		// TODO better cache dir
		mCache = DiskLruCache.open(new File(context.getCacheDir() + "/rapid/" + apiKey), 0, 1, maxSizeInMb * 1_000_000);
	}


	void setMaxSize(int maxSizeInMb) {
		mCache.setMaxSize(maxSizeInMb * 1_000_000);
	}


//	public synchronized String get(Subscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
//		if(!mEnabled)
//			return null;
//		String fingerprint = subscription.getFingerprint();
//		DiskLruCache.Snapshot record = mCache.get(fingerprint);
//		if(record != null) {
//			String jsonValue = record.getString(DEFAULT_INDEX);
//			Logcat.d("Reading from subscription cache. key=%s; value=%s", fingerprint, jsonValue);
//			return jsonValue;
//		}
//		Logcat.d("Reading from disk subscription cache. key=%s; value=null", fingerprint);
//		return null;
//	}


//	public synchronized void put(Subscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
//		if(!mEnabled)
//			return;
//		String fingerprint = subscription.getFingerprint();
//		DiskLruCache.Editor editor = mCache.edit(fingerprint);
//		editor.set(DEFAULT_INDEX, jsonValue);
//		editor.commit();
//		Logcat.d("Saving to disk subscription cache. key=%s; value=%s", fingerprint, jsonValue);
//	}


	public synchronized String get(Subscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
		if(!mEnabled)
			return null;
		String fingerprint = subscription.getFingerprint();
		DiskLruCache.Snapshot record = mCache.get(fingerprint);
		if(record != null) {
			String jsonValue = record.getString(DEFAULT_INDEX);
			Logcat.d("Reading from subscription cache. key=%s; value=%s", fingerprint, jsonValue);
			JSONArray documentIdArray = new JSONArray(jsonValue);
			JSONArray documentArray = new JSONArray();
			for(int i = 0; i < documentIdArray.length(); i++) {
				documentArray.put(new JSONObject(getDocument(documentIdArray.optString(i))));
			}

			return documentArray.toString();
		}
		Logcat.d("Reading from disk subscription cache. key=%s; value=null", fingerprint);
		return null;
	}


	private synchronized String getDocument(String documentId) throws IOException, JSONException, NoSuchAlgorithmException
	{
		DiskLruCache.Snapshot record = mCache.get(documentId);
		if(record != null) {
			String jsonValue = record.getString(DEFAULT_INDEX);
			Logcat.d("Reading from document cache. key=%s; value=%s", documentId, jsonValue);
			return jsonValue;
		}
		Logcat.d("Reading from disk document cache. key=%s; value=null", documentId);
		return null;
	}


	public synchronized void put(Subscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
		if(!mEnabled)
			return;

		JSONArray documentArray = new JSONArray(jsonValue);
		JSONArray documentIdArray = new JSONArray();
		for(int i = 0; i < documentArray.length(); i++) {
			JSONObject document = documentArray.getJSONObject(i);
			String documentId = Sha1Utility.sha1(document.optString(RapidDocument.KEY_ID));
			documentIdArray.put(documentId);
			putDocument(documentId, document.toString());
		}

		String documentIdArrayJson = documentIdArray.toString();
		String fingerprint = subscription.getFingerprint();
		DiskLruCache.Editor editor = mCache.edit(fingerprint);
		editor.set(DEFAULT_INDEX, documentIdArrayJson);
		editor.commit();
		Logcat.d("Saving to disk subscription cache. key=%s; value=%s", fingerprint, documentIdArrayJson);
	}


	private synchronized void putDocument(String documentId, String documentJson) throws IOException, JSONException, NoSuchAlgorithmException {
		DiskLruCache.Editor editor = mCache.edit(documentId);
		editor.set(DEFAULT_INDEX, documentJson);
		editor.commit();
		Logcat.d("Saving to disk document cache. key=%s; value=%s", documentId, documentJson);
	}


	public synchronized void clear() throws IOException {
		mCache.delete();
	}


	synchronized void remove(Subscription subscription) throws IOException, NoSuchAlgorithmException, JSONException {
		if(!mEnabled)
			return;
		String fingerprint = subscription.getFingerprint();
		mCache.remove(fingerprint);
		Logcat.d("Removing from disk subscription cache. key=%s", fingerprint);
	}


	public void setEnabled(boolean cachingEnabled) {
		mEnabled = cachingEnabled;
	}
}

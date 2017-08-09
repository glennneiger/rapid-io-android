package io.rapid;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.rapid.utility.Sha1Utility;


class SubscriptionDiskCache {

	private static final int DEFAULT_INDEX = 0;
	private DiskLruCache mCache;
	private boolean mEnabled = true;


	SubscriptionDiskCache(@NonNull Context context, String apiKey, int maxSizeInMb) throws IOException {
		// TODO better cache dir
		mCache = DiskLruCache.open(new File(context.getCacheDir() + "/rapid/" + apiKey), 0, 1, maxSizeInMb * 1_000_000);
	}


	@Nullable
	public synchronized String get(@NonNull BaseCollectionSubscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
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
				String document = getDocument(subscription, documentIdArray.optString(i));
				if(document == null) {
					return null;
				}
				documentArray.put(new JSONObject(document));
			}

			return documentArray.toString();
		}
		Logcat.d("Reading from disk subscription cache. key=%s; value=null", fingerprint);
		return null;
	}


//	public synchronized String get(BaseCollectionSubscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
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


//	public synchronized void put(BaseCollectionSubscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
//		if(!mEnabled)
//			return;
//		String fingerprint = subscription.getFingerprint();
//		DiskLruCache.Editor editor = mCache.edit(fingerprint);
//		editor.set(DEFAULT_INDEX, jsonValue);
//		editor.commit();
//		Logcat.d("Saving to disk subscription cache. key=%s; value=%s", fingerprint, jsonValue);
//	}


	public synchronized void put(@NonNull BaseCollectionSubscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
		if(!mEnabled)
			return;

		JSONArray documentArray = new JSONArray(jsonValue);
		JSONArray documentIdArray = new JSONArray();
		for(int i = 0; i < documentArray.length(); i++) {
			JSONObject document = documentArray.getJSONObject(i);
			String documentId = Sha1Utility.sha1(document.optString(RapidDocument.KEY_ID));
			documentIdArray.put(documentId);
			putDocument(subscription, documentId, document.toString());
		}

		String documentIdArrayJson = documentIdArray.toString();
		String fingerprint = subscription.getFingerprint();
		DiskLruCache.Editor editor = mCache.edit(fingerprint);
		editor.set(DEFAULT_INDEX, documentIdArrayJson);
		editor.commit();
		Logcat.d("Saving to disk subscription cache. key=%s; value=%s", fingerprint, documentIdArrayJson);
	}


	public synchronized void clear() throws IOException {
		mCache.delete();
	}


	public void setEnabled(boolean cachingEnabled) {
		mEnabled = cachingEnabled;
	}


	void setMaxSize(int maxSizeInMb) {
		mCache.setMaxSize(maxSizeInMb * 1_000_000);
	}


	synchronized void remove(@NonNull BaseCollectionSubscription subscription) throws IOException, NoSuchAlgorithmException, JSONException {
		if(!mEnabled)
			return;
		String fingerprint = subscription.getFingerprint();
		mCache.remove(fingerprint);
		Logcat.d("Removing from disk subscription cache. key=%s", fingerprint);
	}


	private synchronized String getDocument(@NonNull BaseCollectionSubscription subscription, String documentId) throws IOException, JSONException, NoSuchAlgorithmException {
		DiskLruCache.Snapshot record = mCache.get(getDocumentKey(subscription, documentId));
		if(record != null) {
			String jsonValue = record.getString(DEFAULT_INDEX);
			Logcat.d("Reading from document cache. key=%s; value=%s", getDocumentKey(subscription, documentId), jsonValue);
			return jsonValue;
		}
		Logcat.d("Reading from disk document cache. key=%s; value=null", getDocumentKey(subscription, documentId));
		return null;
	}


	private synchronized void putDocument(@NonNull BaseCollectionSubscription subscription, String documentId, String documentJson) throws IOException, JSONException, NoSuchAlgorithmException {
		DiskLruCache.Editor editor = mCache.edit(getDocumentKey(subscription, documentId));
		editor.set(DEFAULT_INDEX, documentJson);
		editor.commit();
		Logcat.d("Saving to disk document cache. key=%s; value=%s", getDocumentKey(subscription, documentId), documentJson);
	}


	private String getDocumentKey(@NonNull BaseCollectionSubscription subscription, String documentId) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return Sha1Utility.sha1(subscription.getCollectionName() + "/" + documentId);
	}
}

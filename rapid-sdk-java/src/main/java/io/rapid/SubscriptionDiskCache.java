package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.rapid.utility.Sha1Utility;


class SubscriptionDiskCache {

	private DiskCache mCache;
	private boolean mEnabled = true;


	SubscriptionDiskCache(DiskCache diskCache){
		mCache = diskCache;
	}


	public synchronized String get(BaseCollectionSubscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
		if(!mEnabled)
			return null;
		String fingerprint = subscription.getFingerprint();
		String jsonValue = mCache.get(fingerprint);
		if(jsonValue != null) {
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


	public synchronized void put(BaseCollectionSubscription subscription, String jsonValue) throws IOException, JSONException, NoSuchAlgorithmException {
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
		mCache.put(fingerprint, documentIdArrayJson);
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


	synchronized void remove(BaseCollectionSubscription subscription) throws IOException, NoSuchAlgorithmException, JSONException {
		if(!mEnabled)
			return;
		String fingerprint = subscription.getFingerprint();
		mCache.remove(fingerprint);
		Logcat.d("Removing from disk subscription cache. key=%s", fingerprint);
	}


	private synchronized String getDocument(BaseCollectionSubscription subscription, String documentId) throws IOException, JSONException, NoSuchAlgorithmException {
		String jsonValue = mCache.get(getDocumentKey(subscription, documentId));
		if(jsonValue != null) {
			Logcat.d("Reading from document cache. key=%s; value=%s", getDocumentKey(subscription, documentId), jsonValue);
			return jsonValue;
		}
		Logcat.d("Reading from disk document cache. key=%s; value=null", getDocumentKey(subscription, documentId));
		return null;
	}


	private synchronized void putDocument(BaseCollectionSubscription subscription, String documentId, String documentJson) throws IOException, JSONException, NoSuchAlgorithmException {
		mCache.put(getDocumentKey(subscription, documentId), documentJson);
		Logcat.d("Saving to disk document cache. key=%s; value=%s", getDocumentKey(subscription, documentId), documentJson);
	}


	private String getDocumentKey(BaseCollectionSubscription subscription, String documentId) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return Sha1Utility.sha1(subscription.getCollectionName() + "/" + documentId);
	}
}

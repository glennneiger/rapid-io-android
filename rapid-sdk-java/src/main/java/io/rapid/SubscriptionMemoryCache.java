package io.rapid;


import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.rapid.utility.Sha1Utility;


class SubscriptionMemoryCache<T> {

	private MemoryCache<List<String>> mSubscriptionCache;
	private MemoryCache<RapidDocument<T>> mDocumentCache;
	private boolean mEnabled = true;


	SubscriptionMemoryCache(CacheProvider cacheProvider) {
		mSubscriptionCache = cacheProvider.getNewMemoryCache(Integer.MAX_VALUE);
		mDocumentCache = cacheProvider.getNewMemoryCache(Integer.MAX_VALUE);
	}


	public synchronized List<RapidDocument<T>> get(BaseCollectionSubscription subscription) throws IOException, JSONException, NoSuchAlgorithmException {
		if(!mEnabled)
			return null;
		String fingerprint = subscription.getFingerprint();
		Logcat.d("Reading from in-memory subscription cache. key: %s", fingerprint);
		List<String> documentIdList = mSubscriptionCache.get(fingerprint);
		List<RapidDocument<T>> documentList = null;
		if(documentIdList != null) {
			documentList = new ArrayList<>();
			for(String documentId : documentIdList) {
				documentList.add(mDocumentCache.get(getDocumentKey(subscription, documentId)));
			}
		}
		return documentList;
	}


	public synchronized void put(BaseCollectionSubscription subscription, List<RapidDocument<T>> value) throws IOException, JSONException, NoSuchAlgorithmException {
		if(!mEnabled)
			return;
		String fingerprint = subscription.getFingerprint();
		Logcat.d("Saving to in-memory subscription cache. key: %s", fingerprint);
		List<String> documentIdList = new ArrayList<>();
		if(value != null) {
			for(RapidDocument<T> document : value) {
				if(document != null) {
					documentIdList.add(document.getId());
					mDocumentCache.put(getDocumentKey(subscription, document.getId()), document);
				}
			}

			mSubscriptionCache.put(fingerprint, documentIdList);
		}
	}


	public synchronized void clear() throws IOException {
		mSubscriptionCache.evictAll();
		mDocumentCache.evictAll();
	}


	public void setEnabled(boolean cachingEnabled) {
		mEnabled = cachingEnabled;
	}


	synchronized void remove(BaseCollectionSubscription subscription) throws IOException, NoSuchAlgorithmException, JSONException {
		if(!mEnabled)
			return;
		String fingerprint = subscription.getFingerprint();
		Logcat.d("Removing from in-memory subscription cache. key: %s", fingerprint);
		mSubscriptionCache.remove(fingerprint);
	}


	private String getDocumentKey(BaseCollectionSubscription subscription, String documentId) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return Sha1Utility.sha1(subscription.getCollectionName() + "/" + documentId);
	}
}

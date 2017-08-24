package io.rapid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rapid.executor.RapidExecutor;
import io.rapid.utility.ModifiableJSONArray;


class WebSocketCollectionConnection<T> implements CollectionConnection<T> {

	private final JsonConverterProvider mJsonConverter;
	@NonNull private final SubscriptionMemoryCache<T> mSubscriptionMemoryCache;
	private final SubscriptionDiskCache mSubscriptionDiskCache;
	private final RapidLogger mLogger;
	private String mCollectionName;
	private RapidConnection mConnection;
	private Class<T> mType;
	private RapidExecutor mExecutor;
	@NonNull private Map<String, BaseCollectionSubscription<T>> mSubscriptions = new HashMap<>();


	WebSocketCollectionConnection(RapidConnection connection, JsonConverterProvider jsonConverter, String collectionName, Class<T> type, SubscriptionDiskCache subscriptionDiskCache, RapidLogger logger, RapidExecutor executor) {
		mCollectionName = collectionName;
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mType = type;
		mExecutor = executor;
		mSubscriptionMemoryCache = new SubscriptionMemoryCache<>(Integer.MAX_VALUE);
		mSubscriptionDiskCache = subscriptionDiskCache;
		mLogger = logger;
	}


	@Override
	public RapidFuture mutate(String id, @Nullable T value, RapidMutateOptions options) {
		RapidDocument<T> doc = new RapidDocument<>(id, value, options);

		if(value == null) {
			return mConnection.delete(mCollectionName, () -> {
				String documentJson = doc.toJson(mJsonConverter);
				mLogger.logI("Deleting document in collection '%s'", mCollectionName);
				mLogger.logJson(documentJson);
				return documentJson;
			});
		} else {
			return mConnection.mutate(mCollectionName, () -> {
				String documentJson = doc.toJson(mJsonConverter);
				mLogger.logI("Mutating document in collection '%s'", mCollectionName);
				mLogger.logJson(documentJson);
				return documentJson;
			});
		}
	}


	@Override
	public RapidFuture merge(String id, Map<String, Object> mergeMap, RapidMutateOptions options) {
		RapidDocument<Map<String, Object>> doc = new RapidDocument<>(id, mergeMap, options);
		return mConnection.merge(mCollectionName, () -> {
			String documentJson = doc.toJson(mJsonConverter);
			mLogger.logI("Merging to document in collection '%s'", mCollectionName);
			mLogger.logJson(documentJson);
			return documentJson;
		});
	}


	@Override
	public void fetch(@NonNull BaseCollectionSubscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		subscription.setSubscribed(true);
		mConnection.fetch(subscriptionId, subscription);
		mSubscriptions.put(subscriptionId, subscription);
	}


	@Override
	public RapidActionFuture onDisconnectMutate(String docId, @Nullable T item, RapidMutateOptions options) {
		RapidDocument<T> doc = new RapidDocument<>(docId, item, options);

		if(item == null) {
			return mConnection.onDisconnectDelete(mCollectionName, () -> {
				String documentJson = doc.toJson(mJsonConverter);
				mLogger.logI("On Disconnect: Deleting document in collection '%s'", mCollectionName);
				mLogger.logJson(documentJson);
				return documentJson;
			});
		} else {
			return mConnection.onDisconnectMutate(mCollectionName, () -> {
				String documentJson = doc.toJson(mJsonConverter);
				mLogger.logI("On Disconnect: Mutating document in collection '%s'", mCollectionName);
				mLogger.logJson(documentJson);
				return documentJson;
			});
		}
	}


	@Override
	public RapidActionFuture onDisconnectMerge(String docId, Map<String, Object> mergeMap, RapidMutateOptions options) {
		RapidDocument<Map<String, Object>> doc = new RapidDocument<>(docId, mergeMap, options);
		return mConnection.onDisconnectMerge(mCollectionName, () -> {
			String documentJson = doc.toJson(mJsonConverter);
			mLogger.logI("On Disconnect: Merging to document in collection '%s'", mCollectionName);
			mLogger.logJson(documentJson);
			return documentJson;
		});
	}


	@Override
	public void subscribe(@NonNull BaseCollectionSubscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		subscription.setSubscriptionId(subscriptionId);
		subscription.setSubscribed(true);

		mLogger.logI("Subscribing to collection '%s'", mCollectionName);

		// send subscribe message only if there is no other subscription with same filter
		try {
			List<BaseCollectionSubscription<T>> identicalSubscriptions = getSubscriptionsWithFingerprint(subscription.getFingerprint());
			if(identicalSubscriptions.isEmpty())
				mConnection.subscribe(subscription);
			else {
				// update the subscription with already existing data
				applyValueToSubscription(subscription, identicalSubscriptions.get(0).getDocuments(), identicalSubscriptions.get(0).getDataState());
			}
		} catch(@NonNull JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			mConnection.subscribe(subscription);
		}

		mSubscriptions.put(subscriptionId, subscription);
		subscription.setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));


		// try to read from cache
		try {
			// first try in-memory cache
			List<RapidDocument<T>> docs = mSubscriptionMemoryCache.get(subscription);
			if(docs != null) {
				if(subscription.getDataState() != BaseCollectionSubscription.DataState.LOADED_FROM_SERVER) {
					mLogger.logI("Value for collection '%s' loaded from in-memory cache", mCollectionName);
					applyValueToSubscription(subscription, docs, BaseCollectionSubscription.DataState.LOADED_FROM_MEMORY_CACHE);
				}
			} else { // then try disk cache
				mExecutor.fetchInBackground(() -> {
					try {
						return mSubscriptionDiskCache.get(subscription);
					} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					return null;
				}, jsonDocs -> {
					if(jsonDocs != null) {
						if(subscription.getDataState() != BaseCollectionSubscription.DataState.LOADED_FROM_SERVER) {
							mLogger.logI("Value for collection '%s' loaded from disk cache", mCollectionName);
							mLogger.logJson(jsonDocs);

							applyValueToSubscription(subscription, jsonDocs, BaseCollectionSubscription.DataState.LOADED_FROM_DISK_CACHE);
						}
					}
				});
			}
		} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onValue(String subscriptionId, @NonNull String documents) {
		mLogger.logI("Collection '%s' value updated", mCollectionName);
		mLogger.logJson(documents);

		BaseCollectionSubscription<T> subscription = mSubscriptions.get(subscriptionId);
		if(subscription != null) {
			try {
				List<BaseCollectionSubscription<T>> identicalSubscriptions = getSubscriptionsWithFingerprint(subscription.getFingerprint());
				for(BaseCollectionSubscription s : identicalSubscriptions) {
					applyValueToSubscription(s, documents, BaseCollectionSubscription.DataState.LOADED_FROM_SERVER);
				}
			} catch(@NonNull JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
				e.printStackTrace();
				applyValueToSubscription(subscription, documents, BaseCollectionSubscription.DataState.LOADED_FROM_SERVER);
			}

			// try to put value to cache
			try {
				// in-memory cache
				mSubscriptionMemoryCache.put(subscription, subscription.getDocuments());

				// disk cache
				mExecutor.doInBackground(() -> {
					try {
						mSubscriptionDiskCache.put(subscription, documents);
					} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				});
			} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void onFetchResult(String fetchId, String documentsJson) {
		BaseCollectionSubscription<T> subscription = mSubscriptions.remove(fetchId);
		subscription.setSubscribed(false);
		applyValueToSubscription(subscription, documentsJson, BaseCollectionSubscription.DataState.LOADED_FROM_SERVER);
	}


	@Override
	public synchronized void onUpdate(String subscriptionId, @NonNull String document) {
		mLogger.logI("Document in collection '%s' updated", mCollectionName);
		mLogger.logJson(document);

		BaseCollectionSubscription<T> subscription = mSubscriptions.get(subscriptionId);
		if(subscription != null) {
			try {
				int documentPosition = -1;
				List<BaseCollectionSubscription<T>> identicalSubscriptions = getSubscriptionsWithFingerprint(subscription.getFingerprint());
				for(BaseCollectionSubscription<T> s : identicalSubscriptions) {
					// apply update to subscription and memory cache
					documentPosition = applyUpdateToSubscription(document, s);
				}

				// apply update to disk cache
				applyUpdateToDiskCache(documentPosition, document, subscription);

			} catch(@NonNull JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
				e.printStackTrace();
				applyUpdateToSubscription(document, subscription);
			}
		}
	}


	@Override
	public void onRemove(String subscriptionId, @NonNull String document) {
		onUpdate(subscriptionId, document);
	}


	@Override
	public void onError(String subscriptionId, RapidError error) {
		BaseCollectionSubscription<T> subscription = mSubscriptions.get(subscriptionId);
		if(subscription != null) {
			subscription.invokeError(error);
			mSubscriptions.remove(subscription.getSubscriptionId());
			subscription.setSubscribed(false);
		}
	}


	@Override
	public void onTimedOut() {
		for(BaseCollectionSubscription<T> subscription : mSubscriptions.values()) {
			RapidError error = new RapidError(RapidError.ErrorType.TIMEOUT);
			mLogger.logE(error);
			subscription.invokeError(error);
			mSubscriptions.remove(subscription.getSubscriptionId());
		}
	}


	@Override
	public boolean hasActiveSubscription() {
		for(BaseCollectionSubscription<T> subscription : mSubscriptions.values()) {
			if(subscription.isSubscribed())
				return true;
		}
		return false;
	}


	@Override
	public void resubscribe() {
		for(Map.Entry<String, BaseCollectionSubscription<T>> subscriptionEntry : mSubscriptions.entrySet()) {
			BaseCollectionSubscription<T> subscription = subscriptionEntry.getValue();
			mConnection.subscribe(subscription);
		}
	}


	private int applyUpdateToSubscription(String document, @NonNull BaseCollectionSubscription<T> subscription) {
		int documentPosition = subscription.onDocumentUpdated(parseDocument(document));

		// try to update in-memory cache
		try {

			mSubscriptionMemoryCache.put(subscription, subscription.getDocuments());
		} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
			Logcat.d("Unable to update subscription cache. Need to remove this subscription from cache.");
			e.printStackTrace();
			// unable to update data in cache - cache inconsistent -> clear it
			try {
				mSubscriptionMemoryCache.remove(subscription);
			} catch(@NonNull IOException | NoSuchAlgorithmException | JSONException e1) {
				e1.printStackTrace();
			}
		}
		return documentPosition;
	}


	private void applyUpdateToDiskCache(int documentPosition, String document, @NonNull BaseCollectionSubscription<T> subscription) {
		try {
			// disk cache
			String jsonDocs = mSubscriptionDiskCache.get(subscription);
			if(jsonDocs != null) {
				JSONObject updatedDoc = new JSONObject(document);
				String updatedDocId = updatedDoc.getString(RapidDocument.KEY_ID);

				ModifiableJSONArray currentItems = new ModifiableJSONArray(jsonDocs);

				if(!updatedDoc.has(RapidDocument.KEY_BODY)) {
					for(int i = 0; i < currentItems.length(); i++) {
						String docId = currentItems.getJSONObject(i).getString(RapidDocument.KEY_ID);
						if(docId.equals(updatedDocId)) {
							currentItems = ModifiableJSONArray.removeItem(currentItems, i);
							break;
						}
					}
				} else {
					int oldDocumentPosition = -1;
					for(int i = 0; i < currentItems.length(); i++) {
						String docId = currentItems.getJSONObject(i).getString(RapidDocument.KEY_ID);
						if(docId.equals(updatedDocId)) {
							oldDocumentPosition = i;
							break;
						}
					}
					if(oldDocumentPosition != -1) {
						currentItems = ModifiableJSONArray.removeItem(currentItems, oldDocumentPosition);
					}
					currentItems.add(documentPosition, updatedDoc);
				}
				ModifiableJSONArray finalCurrentItems = currentItems;

				// update disk cache
				mExecutor.doInBackground(() -> {
					try {
						mSubscriptionDiskCache.put(subscription, finalCurrentItems.toString());
					} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
						e.printStackTrace();
						// unable to update data in cache - cache inconsistent -> clear it
						try {
							mSubscriptionDiskCache.remove(subscription);
						} catch(@NonNull IOException | NoSuchAlgorithmException | JSONException e1) {
							e1.printStackTrace();
						}
					}
				});
			}

		} catch(@NonNull IOException | JSONException | NoSuchAlgorithmException e) {
			Logcat.d("Unable to update disk subscription cache.");
			e.printStackTrace();
		}
	}


	private void applyValueToSubscription(BaseCollectionSubscription subscription, String documents, BaseCollectionSubscription.DataState dataState) {
		List<RapidDocument<T>> parsedDocs = parseDocumentList(documents);
		if(subscription instanceof RapidDocumentSubscription) {
			((RapidDocumentSubscription) subscription).setDocument(parsedDocs.isEmpty() ? null : parsedDocs.get(0), dataState);
		} else if(subscription instanceof RapidCollectionSubscription) {
			((RapidCollectionSubscription) subscription).setDocuments(parsedDocs, dataState);
		}
	}


	private void applyValueToSubscription(BaseCollectionSubscription subscription, @NonNull List<RapidDocument<T>> documents, BaseCollectionSubscription.DataState dataState) {
		if(subscription instanceof RapidDocumentSubscription) {
			((RapidDocumentSubscription) subscription).setDocument(documents.isEmpty() ? null : documents.get(0), dataState);
		} else if(subscription instanceof RapidCollectionSubscription) {
			((RapidCollectionSubscription) subscription).setDocuments(documents, dataState);
		}
	}


	@NonNull
	private synchronized List<BaseCollectionSubscription<T>> getSubscriptionsWithFingerprint(String fingerprint) {
		ArrayList<BaseCollectionSubscription<T>> list = new ArrayList<>();
		for(BaseCollectionSubscription<T> s : mSubscriptions.values()) {
			try {
				if(s.getFingerprint().equals(fingerprint))
					list.add(s);
			} catch(@NonNull JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return list;
	}


	private void onSubscriptionUnsubscribed(@NonNull BaseCollectionSubscription<T> subscription) {
		mLogger.logI("Unsubscribing from collection '%s'", mCollectionName);

		mSubscriptions.remove(subscription.getSubscriptionId());
		mConnection.onUnsubscribe(subscription);
	}


	@NonNull
	private List<RapidDocument<T>> parseDocumentList(String documents) {
		List<RapidDocument<T>> list = new ArrayList<>();
		try {
			JSONArray array = new JSONArray(documents);
			for(int i = 0; i < array.length(); i++) {
				RapidDocument<T> doc = parseDocument(array.optString(i));
				list.add(doc);
			}
			return list;
		} catch(JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}


	private RapidDocument<T> parseDocument(String document) {
		try {
			JSONObject jsonObject = new JSONObject(document);
			return RapidDocument.fromJsonObject(jsonObject, mJsonConverter, mType);
		} catch(@NonNull IOException | JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

}

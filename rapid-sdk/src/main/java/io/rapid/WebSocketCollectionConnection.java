package io.rapid;


import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rapid.converter.RapidJsonConverter;
import io.rapid.utility.ModifiableJSONArray;


class WebSocketCollectionConnection<T> implements CollectionConnection<T> {

	private final RapidJsonConverter mJsonConverter;
	private final SubscriptionMemoryCache<T> mSubscriptionMemoryCache;
	private final SubscriptionDiskCache mSubscriptionDiskCache;
	private String mCollectionName;
	private RapidConnection mConnection;
	private Class<T> mType;
	private Map<String, Subscription<T>> mSubscriptions = new HashMap<>();


	WebSocketCollectionConnection(RapidConnection connection, RapidJsonConverter jsonConverter, String collectionName, Class<T> type, SubscriptionDiskCache subscriptionDiskCache) {
		mCollectionName = collectionName;
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mType = type;
		mSubscriptionMemoryCache = new SubscriptionMemoryCache<>(10);
		mSubscriptionDiskCache = subscriptionDiskCache;
	}


	@Override
	public RapidFuture mutate(String id, T value) {

		// TODO
		if(IndexCache.getInstance().get(mType.getName()) == null) {
			List<String> indexList = new ArrayList<>();
			for(Field f : mType.getDeclaredFields()) {
				if(f.isAnnotationPresent(Index.class)) {
					if(f.isAnnotationPresent(SerializedName.class)) {
						indexList.add(f.getAnnotation(SerializedName.class).value());
					} else {
						String indexName = f.getAnnotation(Index.class).value();
						indexList.add(indexName.isEmpty() ? f.getName() : indexName);
					}
					Logcat.d(indexList.get(indexList.size() - 1));
				}
			}
			IndexCache.getInstance().put(mType.getName(), indexList);
		}

		RapidDocument<T> doc = new RapidDocument<>(id, value);
		return mConnection.mutate(mCollectionName, () -> toJson(doc));
	}


	@Override
	public void subscribe(Subscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		subscription.setSubscriptionId(subscriptionId);

		// send subscribe message only if there is no other subscription with same filter
		try {
			List<Subscription<T>> identicalSubscriptions = getSubscriptionsWithFingerprint(subscription.getFingerprint());
			if(identicalSubscriptions.isEmpty())
				mConnection.subscribe(subscriptionId, subscription);
			else {
				// update the subscription with already existing data
				applyValueToSubscription(subscription, ((RapidCollectionSubscription<T>) identicalSubscriptions.get(0)).getDocuments(), true);
			}
		} catch(JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			mConnection.subscribe(subscriptionId, subscription);
		}

		mSubscriptions.put(subscriptionId, subscription);
		if(subscription instanceof RapidCollectionSubscription)
			((RapidCollectionSubscription) subscription).setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));
		else if(subscription instanceof RapidDocumentSubscription)
			((RapidDocumentSubscription) subscription).setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));


		// try to read from cache
		try {
			// first try in-memory cache
			List<RapidDocument<T>> docs = mSubscriptionMemoryCache.get(subscription);
			if(docs != null) {
				applyValueToSubscription(subscription, docs, true);
			} else { // then try disk cache
				String jsonDocs = mSubscriptionDiskCache.get(subscription);
				if(jsonDocs != null)
					applyValueToSubscription(subscription, jsonDocs, true);
			}
		} catch(IOException | JSONException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onValue(String subscriptionId, String documents) {
		Subscription<T> subscription = mSubscriptions.get(subscriptionId);
		try {
			List<Subscription<T>> identicalSubscriptions = getSubscriptionsWithFingerprint(subscription.getFingerprint());
			for(Subscription s : identicalSubscriptions) {
				applyValueToSubscription(s, documents, false);
			}
		} catch(JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			applyValueToSubscription(subscription, documents, false);
		}

		// try to put value to cache
		try {
			// in-memory cache
			mSubscriptionMemoryCache.put(subscription, subscription.getDocuments());

			// disk cache
			mSubscriptionDiskCache.put(subscription, documents);
		} catch(IOException | JSONException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}


	@Override
	public synchronized void onUpdate(String subscriptionId, String previousSiblingId, String document) {
		Subscription<T> subscription = mSubscriptions.get(subscriptionId);
		try {
			List<Subscription<T>> identicalSubscriptions = getSubscriptionsWithFingerprint(subscription.getFingerprint());
			for(Subscription<T> s : identicalSubscriptions) {
				applyUpdateToSubscription(previousSiblingId, document, s);
			}
		} catch(JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			applyUpdateToSubscription(previousSiblingId, document, subscription);
		}
	}


	@Override
	public void onError(String subscriptionId, RapidError error) {
		Subscription<T> subscription = mSubscriptions.get(subscriptionId);
		subscription.invokeError(error);
		mSubscriptions.remove(subscription.getSubscriptionId());
	}


	@Override
	public void onTimedOut() {
		for(Subscription<T> subscription : mSubscriptions.values()) {
			subscription.invokeError(new RapidError(RapidError.TIMEOUT));
			mSubscriptions.remove(subscription.getSubscriptionId());
		}
	}


	@Override
	public boolean hasActiveSubscription() {
		for(Subscription<T> subscription : mSubscriptions.values()) {
			if(subscription.isSubscribed())
				return true;
		}
		return false;
	}


	@Override
	public void resubscribe() {
		for(Map.Entry<String, Subscription<T>> subscriptionEntry : mSubscriptions.entrySet()) {
			Subscription<T> subscription = subscriptionEntry.getValue();
			mConnection.subscribe(subscriptionEntry.getKey(), subscription);
		}
	}


	private void applyUpdateToSubscription(String previousSiblingId, String document, Subscription<T> subscription) {
		subscription.onDocumentUpdated(previousSiblingId, parseDocument(document));

		// try to update cache
		try {
			// in-memory cache
			mSubscriptionMemoryCache.put(subscription, subscription.getDocuments());

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
					int previousSiblingPosition = -1;
					int documentPosition = -1;
					for(int i = 0; i < currentItems.length(); i++) {
						String docId = currentItems.getJSONObject(i).getString(RapidDocument.KEY_ID);
						if(docId.equals(previousSiblingId)) {
							previousSiblingPosition = i;
						} else if(docId.equals(updatedDocId)) {
							documentPosition = i;
						}
					}
					if(documentPosition != -1) {
						currentItems = ModifiableJSONArray.removeItem(currentItems, documentPosition);

						if(documentPosition < previousSiblingPosition)
							currentItems.add(previousSiblingPosition, updatedDoc);
						else
							currentItems.add(previousSiblingPosition + 1, updatedDoc);
					}
					else {
						currentItems.add(previousSiblingPosition + 1, updatedDoc);
					}
				}

				mSubscriptionDiskCache.put(subscription, currentItems.toString());
			}

		} catch(IOException | JSONException | NoSuchAlgorithmException e) {
			Logcat.d("Unable to update subscription cache. Need to remove this subscription from cache.");
			e.printStackTrace();
			// unable to update data in cache - cache inconsistent -> clear it
			try {
				mSubscriptionDiskCache.remove(subscription);
			} catch(IOException | NoSuchAlgorithmException | JSONException e1) {
				e1.printStackTrace();
			}
		}
	}


	private void applyValueToSubscription(Subscription subscription, String documents, boolean fromCache) {
		if(subscription instanceof RapidDocumentSubscription) {
			((RapidDocumentSubscription) subscription).setDocument(parseDocumentList(documents).get(0));
		} else if(subscription instanceof RapidCollectionSubscription) {
			((RapidCollectionSubscription) subscription).setDocuments(parseDocumentList(documents), fromCache);
		}
	}


	private void applyValueToSubscription(Subscription subscription, List<RapidDocument<T>> documents, boolean fromCache) {
		if(subscription instanceof RapidDocumentSubscription) {
			((RapidDocumentSubscription) subscription).setDocument(documents.get(0));
		} else if(subscription instanceof RapidCollectionSubscription) {
			((RapidCollectionSubscription) subscription).setDocuments(documents, fromCache);
		}
	}


	private List<Subscription<T>> getSubscriptionsWithFingerprint(String fingerprint) {
		ArrayList<Subscription<T>> list = new ArrayList<>();
		for(Subscription<T> s : mSubscriptions.values()) {
			try {
				if(s.getFingerprint().equals(fingerprint))
					list.add(s);
			} catch(JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return list;
	}


	private void onSubscriptionUnsubscribed(Subscription<T> subscription) {
		mSubscriptions.remove(subscription.getSubscriptionId());
		mConnection.onUnsubscribe(subscription);
	}


	private String toJson(RapidDocument<T> document) {
		try {
			return mJsonConverter.toJson(document);
		} catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
	}


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
		} catch(IOException | JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

}

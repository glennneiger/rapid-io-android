package io.rapid;


import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rapid.converter.RapidJsonConverter;


class WebSocketCollectionConnection<T> implements CollectionConnection<T> {

	private final RapidJsonConverter mJsonConverter;
	private String mCollectionName;
	private RapidConnection mConnection;
	private Class<T> mType;
	private Map<String, Subscription<T>> mSubscriptions = new HashMap<>();


	WebSocketCollectionConnection(RapidConnection connection, RapidJsonConverter jsonConverter, String collectionName, Class<T> type) {
		mCollectionName = collectionName;
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mType = type;
	}


	@Override
	public RapidFuture mutate(String id, T value) {

		// TODO

		if(Index.Cache.getInstance().get(mType.getName()) == null)
		{
			List<String> indexList = new ArrayList<>();
			for(Field f : mType.getDeclaredFields())
			{
				if(f.isAnnotationPresent(Index.class))
				{
					if(f.isAnnotationPresent(SerializedName.class))
					{
						indexList.add(f.getAnnotation(SerializedName.class).value());
					}
					else
					{
						String indexName = f.getAnnotation(Index.class).value();
						indexList.add(indexName.isEmpty() ? f.getName() : indexName);
					}
					Logcat.d(indexList.get(indexList.size()-1));
				}
			}
			Index.Cache.getInstance().put(mType.getName(), indexList);
		}

		RapidDocument<T> doc = new RapidDocument<>(id, value);
		return mConnection.mutate(mCollectionName, toJson(doc));
	}


	@Override
	public void subscribe(RapidCollectionSubscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		subscription.setSubscriptionId(subscriptionId);
		mConnection.subscribe(subscriptionId, subscription);
		mSubscriptions.put(subscriptionId, subscription);
		subscription.setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));
	}


	@Override
	public void subscribeDocument(RapidDocumentSubscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		subscription.setSubscriptionId(subscriptionId);
		mConnection.subscribe(subscriptionId, subscription);
		mSubscriptions.put(subscriptionId, subscription);
		subscription.setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));
	}


	@Override
	public void onValue(String subscriptionId, String documents) {
		Subscription<T> subscription = mSubscriptions.get(subscriptionId);
		if(subscription instanceof RapidDocumentSubscription) {
			((RapidDocumentSubscription) subscription).setDocument(parseDocumentList(documents).get(0));
		} else if(subscription instanceof RapidCollectionSubscription) {
			((RapidCollectionSubscription) subscription).setDocuments(parseDocumentList(documents));
		}
	}


	@Override
	public void onUpdate(String subscriptionId, String document) {
		Subscription<T> subscription = mSubscriptions.get(subscriptionId);
		subscription.onDocumentUpdated(parseDocument(document));
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


	private void onSubscriptionUnsubscribed(Subscription<T> subscription) {
		mSubscriptions.remove(subscription);
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

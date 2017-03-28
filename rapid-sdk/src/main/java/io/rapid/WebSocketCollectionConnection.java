package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class WebSocketCollectionConnection<T> implements CollectionConnection<T> {

	private String mCollectionName;
	private Rapid mRapid;
	private Class<T> mType;
	private Map<String, Subscription<T>> mSubscriptions = new HashMap<>();


	WebSocketCollectionConnection(Rapid rapid, String collectionName, Class<T> type) {
		mCollectionName = collectionName;
		mRapid = rapid;
		mType = type;
	}


	@Override
	public RapidFuture<T> mutate(String id, T value) {
		RapidFuture<T> future = new RapidFuture<>();

		RapidDocument<T> doc = new RapidDocument<>(id, value);
		mRapid.sendMessage(new MessageMut(IdProvider.getNewEventId(), mCollectionName, toJson(doc))).onSuccess(future::invokeSuccess);

		return future;
	}


	@Override
	public void subscribe(RapidCollectionSubscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		MessageSub subscriptionMsg = subscription.createSubscriptionMessage(subscriptionId);
		mRapid.onSubscribe(subscription);
		mRapid.sendMessage(subscriptionMsg);
		mSubscriptions.put(subscriptionId, subscription);
		subscription.setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));
	}


	@Override
	public void subscribeDocument(RapidDocumentSubscription<T> subscription) {
		String subscriptionId = IdProvider.getNewSubscriptionId();
		MessageSub subscriptionMsg = subscription.createSubscriptionMessage(subscriptionId);
		mRapid.onSubscribe(subscription);
		mRapid.sendMessage(subscriptionMsg);
		mSubscriptions.put(subscriptionId, subscription);
		subscription.setOnUnsubscribeCallback(() -> onSubscriptionUnsubscribed(subscription));
	}


	@Override
	public void onValue(MessageVal valMessage) {
		Subscription<T> subscription = mSubscriptions.get(valMessage.getSubscriptionId());
		if(subscription instanceof RapidDocumentSubscription) {
			((RapidDocumentSubscription) subscription).setDocument(parseDocumentList(valMessage.getDocuments()).get(0));
		} else if(subscription instanceof RapidCollectionSubscription) {
			((RapidCollectionSubscription) subscription).setDocuments(parseDocumentList(valMessage.getDocuments()));
		}
	}


	@Override
	public void onUpdate(MessageUpd updMessage) {
		Subscription<T> subscription = mSubscriptions.get(updMessage.getSubscriptionId());
		subscription.onDocumentUpdated(parseDocument(updMessage.getDocument()));
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
			MessageSub subscriptionMsg = subscription.createSubscriptionMessage(subscriptionEntry.getKey());
			mRapid.sendMessage(subscriptionMsg);
		}
	}


	private void onSubscriptionUnsubscribed(Subscription<T> subscription) {
		mSubscriptions.remove(subscription);
		mRapid.onUnsubscribe(subscription);
	}


	private String toJson(RapidDocument<T> document) {
		try {
			return mRapid.getJsonConverter().toJson(document);
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
			return RapidDocument.fromJsonObject(jsonObject, mRapid.getJsonConverter(), mType);
		} catch(IOException | JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

}

package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class WebSocketCollectionConnection<T> implements CollectionConnection<T> {

	private String mCollectionName;
	private Rapid mRapid;
	private Class<T> mType;
	private Set<RapidSubscription<T>> mSubscriptions = new HashSet<>();

	private List<RapidDocument<T>> mCollection = new ArrayList<>();


	WebSocketCollectionConnection(Rapid rapid, String collectionName, Class<T> type) {
		mCollectionName = collectionName;
		mRapid = rapid;
		mType = type;
	}


	@Override
	public RapidFuture<T> mutate(String id, T value) {
		RapidFuture<T> future = new RapidFuture<>();

		RapidDocument<T> doc = new RapidDocument<>(id, value);
		mRapid.sendMessage(new MessageMut(IdProvider.getNewEventId(), mCollectionName, toJson(doc)));
		future.invokeSuccess(); // TODO: Call this when ACK is received for this message

		return future;
	}


	@Override
	public RapidSubscription subscribe(RapidCollectionCallback<T> callback) {
		MessageSub subscriptionMsg = new MessageSub(IdProvider.getNewEventId(), mCollectionName, IdProvider.getNewSubscriptionId());
		// TODO
		subscriptionMsg.setSkip(10);
		subscriptionMsg.setLimit(10);
		mRapid.sendMessage(subscriptionMsg);

		RapidSubscription<T> subscription = new RapidSubscription<>(callback);
		mSubscriptions.add(subscription);
		subscription.setOnUnsubscribeCallback(() -> mSubscriptions.remove(subscription));
		return subscription;
	}


	@Override
	public RapidSubscription subscribeDocument(RapidDocumentCallback<T> callback) {
		// TODO
		return null;
	}


	@Override
	public void onValue(MessageVal valMessage) {
		mCollection = parseDocumentList(valMessage.getDocuments());
		notifyChange();
	}


	@Override
	public void onUpdate(MessageUpd updMessage) {
		RapidDocument<T> doc = parseDocument(updMessage.getDocument());
		boolean modified = false;
		for(int i = 0; i < mCollection.size(); i++) {
			if(mCollection.get(i).getId().equals(doc.getId())) {
				mCollection.set(i, doc);
				modified = true;
				break;
			}
		}
		if(!modified) {
			mCollection.add(doc);
		}
		notifyChange();
	}


	private String toJson(RapidDocument<T> document) {
		try {
			return mRapid.getJsonConverter().toJson(document);
		} catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
	}


	private void notifyChange() {
		for(RapidSubscription<T> subscription : mSubscriptions) {
			// deliver result on UI thread
			mRapid.getHandler().post(() -> {
				subscription.invokeChange(new ArrayList<>(mCollection));
			});
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

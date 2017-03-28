package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class WebSocketCollectionConnection<T> implements CollectionConnection<T> {

	private String mCollectionName;
	private Rapid mRapid;
	private Class<T> mType;
	private RapidCollectionSubscription<T> mCollectionSubscription;
	private RapidDocumentSubscription<T> mDocumentSubscription;

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
		mRapid.sendMessage(new MessageMut(IdProvider.getNewEventId(), mCollectionName, toJson(doc))).onSuccess(future::invokeSuccess);

		return future;
	}


	@Override
	public RapidCollectionSubscription subscribe(RapidCollectionCallback<T> callback, EntityOrder order, int limit, int skip, Filter filter) {
		checkNotSubscribed();
		MessageSub subscriptionMsg = createSubscriptionMessage(order, limit, skip, filter);
		RapidCollectionSubscription<T> subscription = new RapidCollectionSubscription<>(callback);
		mRapid.onSubscribe(subscription);
		mRapid.sendMessage(subscriptionMsg);
		mCollectionSubscription = subscription;
		subscription.setOnUnsubscribeCallback(() -> onCollectionUnsubscribed(subscription));
		return subscription;
	}


	@Override
	public RapidDocumentSubscription subscribeDocument(String id, RapidDocumentCallback<T> callback) {
		checkNotSubscribed();
		MessageSub subscriptionMsg = createSubscriptionMessage(null, 1, 0, new FilterValue(Config.ID_IDENTIFIER, new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, id)));
		RapidDocumentSubscription<T> subscription = new RapidDocumentSubscription<>(callback);
		mRapid.onSubscribe(subscription);
		mRapid.sendMessage(subscriptionMsg);
		mDocumentSubscription = subscription;
		subscription.setOnUnsubscribeCallback(() -> onDocumentUnsubscribed(subscription));
		return subscription;
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


	@Override
	public boolean isSubscribed() {
		return mCollectionSubscription != null || mDocumentSubscription != null;
	}


	@Override
	public void resubscribe(EntityOrder order, int limit, int skip, Filter filter) {
		MessageSub subscriptionMsg = createSubscriptionMessage(order, limit, skip, filter);
		mRapid.sendMessage(subscriptionMsg);
	}


	private void checkNotSubscribed() {
		if(mDocumentSubscription != null || mCollectionSubscription != null) {
			throw new IllegalStateException("This collection/document is already subscribed.");
		}
	}


	private void onCollectionUnsubscribed(RapidCollectionSubscription<T> subscription) {
		mRapid.onUnsubscribe(subscription);
	}


	private void onDocumentUnsubscribed(RapidDocumentSubscription<T> subscription) {
		mRapid.onUnsubscribe(subscription);
	}


	private String toJson(RapidDocument<T> document) {
		try {
			return mRapid.getJsonConverter().toJson(document);
		} catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
	}


	private void notifyChange() {
		if(mCollectionSubscription != null) {
			// deliver result on UI thread
			mRapid.getHandler().post(() -> {
				mCollectionSubscription.invokeChange(new ArrayList<>(mCollection));
			});
		} else if(mDocumentSubscription != null) {
			// deliver result on UI thread
			mRapid.getHandler().post(() -> {
				mDocumentSubscription.invokeChange(mCollection.get(0));
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


	private MessageSub createSubscriptionMessage(EntityOrder order, int limit, int skip, Filter filter) {
		MessageSub subscriptionMsg = new MessageSub(IdProvider.getNewEventId(), mCollectionName, IdProvider.getNewSubscriptionId());
		subscriptionMsg.setSkip(skip);
		subscriptionMsg.setLimit(limit);
		subscriptionMsg.setOrder(order);
		subscriptionMsg.setFilter(filter);
		return subscriptionMsg;
	}
}

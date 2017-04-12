package io.rapid;


import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import io.rapid.converter.RapidJsonConverter;


class CollectionProvider {
	private final Handler mOriginalThreadHandler;
	private final RapidJsonConverter mJsonConverter;
	private final SubscriptionCache mSubscriptionCache;
	RapidConnection mConnection;
	private Map<String, RapidCollectionReference> mCollections = new HashMap<>();


	public CollectionProvider(RapidConnection connection, RapidJsonConverter jsonConverter, Handler originalThreadHandler, SubscriptionCache subscriptionCache) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mOriginalThreadHandler = originalThreadHandler;
		mSubscriptionCache = subscriptionCache;
	}


	public SubscriptionCache getSubscriptionCache() {
		return mSubscriptionCache;
	}


	<T> RapidCollectionReference<T> provideCollection(String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<>(new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, itemClass, mSubscriptionCache), collectionName, mOriginalThreadHandler));
		return mCollections.get(collectionName);
	}


	RapidCollectionReference findCollectionByName(String collectionName) {
		return mCollections.get(collectionName);
	}


	Map<String, RapidCollectionReference> getCollections() {
		return mCollections;
	}


	void resubscribeAll() {
		for(RapidCollectionReference rapidCollectionReference : getCollections().values()) {
			if(rapidCollectionReference.isSubscribed()) {
				rapidCollectionReference.resubscribe();
			}
		}
	}


	void timedOutAll() {
		for(RapidCollectionReference rapidCollectionReference : getCollections().values()) {
			if(rapidCollectionReference.isSubscribed()) {
				rapidCollectionReference.onTimedOut();
			}
		}
	}
}
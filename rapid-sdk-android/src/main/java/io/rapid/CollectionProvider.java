package io.rapid;


import android.os.Handler;

import java.util.HashMap;
import java.util.Map;


class CollectionProvider {
	private final Handler mOriginalThreadHandler;
	private final JsonConverterProvider mJsonConverter;
	private final SubscriptionDiskCache mSubscriptionDiskCache;
	private final RapidLogger mDebugLogger;
	private RapidConnection mConnection;
	private Map<String, RapidCollectionReference> mCollections = new HashMap<>();


	CollectionProvider(RapidConnection connection, JsonConverterProvider jsonConverter, Handler originalThreadHandler, SubscriptionDiskCache subscriptionDiskCache, RapidLogger debugLogger) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mOriginalThreadHandler = originalThreadHandler;
		mSubscriptionDiskCache = subscriptionDiskCache;
		mDebugLogger = debugLogger;
	}


	SubscriptionDiskCache getSubscriptionDiskCache() {
		return mSubscriptionDiskCache;
	}


	<T> RapidCollectionReference<T> provideCollection(String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<>(new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, itemClass, mSubscriptionDiskCache, mDebugLogger), collectionName, mOriginalThreadHandler, mJsonConverter));
		return mCollections.get(collectionName);
	}


	RapidCollectionReference<Map<String, Object>> provideCollection(String collectionName) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<>(new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, Map.class, mSubscriptionDiskCache, mDebugLogger), collectionName, mOriginalThreadHandler, mJsonConverter));
		return mCollections.get(collectionName);
	}


	RapidCollectionReference findCollectionByName(String collectionName) {
		return mCollections.get(collectionName);
	}


	void resubscribeAll() {
		for(RapidCollectionReference rapidCollectionReference : mCollections.values()) {
			if(rapidCollectionReference.isSubscribed()) {
				rapidCollectionReference.resubscribe();
			}
		}
	}


	void timedOutAll() {
		for(RapidCollectionReference rapidCollectionReference : mCollections.values()) {
			if(rapidCollectionReference.isSubscribed()) {
				rapidCollectionReference.onTimedOut();
			}
		}
	}
}
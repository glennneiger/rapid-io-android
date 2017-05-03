package io.rapid;


import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import io.rapid.converter.RapidJsonConverter;


class CollectionProvider {
	private final Handler mOriginalThreadHandler;
	private final RapidJsonConverter mJsonConverter;
	private final SubscriptionDiskCache mSubscriptionDiskCache;
	private final RapidLogger mDebugLogger;
	RapidConnection mConnection;
	private Map<String, RapidCollectionReference> mCollections = new HashMap<>();


	public CollectionProvider(RapidConnection connection, RapidJsonConverter jsonConverter, Handler originalThreadHandler, SubscriptionDiskCache subscriptionDiskCache, RapidLogger debugLogger) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mOriginalThreadHandler = originalThreadHandler;
		mSubscriptionDiskCache = subscriptionDiskCache;
		mDebugLogger = debugLogger;
	}


	public SubscriptionDiskCache getSubscriptionDiskCache() {
		return mSubscriptionDiskCache;
	}


	<T> RapidCollectionReference<T> provideCollection(String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<>(new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, itemClass, mSubscriptionDiskCache, mDebugLogger), collectionName, mOriginalThreadHandler, mJsonConverter));
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
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
	private Map<String, CollectionConnection> mCollectionConnections = new HashMap<>();
	private Map<String, ChannelConnection> mChannelConnections = new HashMap<>();


	CollectionProvider(RapidConnection connection, JsonConverterProvider jsonConverter, Handler originalThreadHandler, SubscriptionDiskCache subscriptionDiskCache, RapidLogger debugLogger) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mOriginalThreadHandler = originalThreadHandler;
		mSubscriptionDiskCache = subscriptionDiskCache;
		mDebugLogger = debugLogger;
	}


	public ChannelConnection findChannelByName(String channelName) {
		return mChannelConnections.get(channelName);
	}


	<T> RapidChannelReference<T> provideChannel(String channelName, Class<T> messageClass) {
		if(!mChannelConnections.containsKey(channelName))
			mChannelConnections.put(channelName, new WebSocketChannelConnection<>(mConnection, mJsonConverter, channelName, messageClass, mDebugLogger));
		return new RapidChannelReference<>(mChannelConnections.get(channelName), channelName, mOriginalThreadHandler);
	}


	SubscriptionDiskCache getSubscriptionDiskCache() {
		return mSubscriptionDiskCache;
	}


	<T> RapidCollectionReference<T> provideCollection(String collectionName, Class<T> itemClass) {
		if(!mCollectionConnections.containsKey(collectionName))
			mCollectionConnections.put(collectionName, new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, itemClass, mSubscriptionDiskCache, mDebugLogger));
		return new RapidCollectionReference<>(mCollectionConnections.get(collectionName), collectionName, mOriginalThreadHandler, mJsonConverter);
	}


	RapidCollectionReference<Map<String, Object>> provideCollection(String collectionName) {
		if(!mCollectionConnections.containsKey(collectionName))
			mCollectionConnections.put(collectionName, new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, Map.class, mSubscriptionDiskCache, mDebugLogger));
		return new RapidCollectionReference<>(mCollectionConnections.get(collectionName), collectionName, mOriginalThreadHandler, mJsonConverter);
	}


	CollectionConnection findCollectionByName(String collectionName) {
		return mCollectionConnections.get(collectionName);
	}


	void resubscribeAll() {
		for(CollectionConnection conn : mCollectionConnections.values()) {
			if(conn.hasActiveSubscription()) {
				conn.resubscribe();
			}
		}
	}


	void timedOutAll() {
		for(CollectionConnection conn : mCollectionConnections.values()) {
			if(conn.hasActiveSubscription()) {
				conn.onTimedOut();
			}
		}
	}
}
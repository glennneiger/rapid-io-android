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
	private Map<String, ChannelConnection> mChannelPrefixConnections = new HashMap<>();


	CollectionProvider(RapidConnection connection, JsonConverterProvider jsonConverter, Handler originalThreadHandler, SubscriptionDiskCache subscriptionDiskCache, RapidLogger debugLogger) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mOriginalThreadHandler = originalThreadHandler;
		mSubscriptionDiskCache = subscriptionDiskCache;
		mDebugLogger = debugLogger;
	}


	public ChannelConnection findChannelBySubscriptionId(String subscriptionId) {
		for(String channelName : mChannelConnections.keySet()) {
			if (mChannelConnections.get(channelName).hasSubscription(subscriptionId))
				return mChannelConnections.get(channelName);
		}
		for(String channelName : mChannelPrefixConnections.keySet()) {
			if (mChannelPrefixConnections.get(channelName).hasSubscription(subscriptionId))
				return mChannelPrefixConnections.get(channelName);
		}
		throw new IllegalArgumentException("BaseCollectionSubscription not found");
	}


	<T> RapidChannelReference<T> provideChannel(String channelName, Class<T> messageClass, boolean nameIsPrefix) {
		if(nameIsPrefix) {
			if(!mChannelPrefixConnections.containsKey(channelName))
				mChannelPrefixConnections.put(channelName, new WebSocketChannelConnection<>(mConnection, mJsonConverter, channelName, messageClass, mDebugLogger, true));
		} else {
			if(!mChannelConnections.containsKey(channelName))
				mChannelConnections.put(channelName, new WebSocketChannelConnection<>(mConnection, mJsonConverter, channelName, messageClass, mDebugLogger, false));
		}
		return new RapidChannelReference<T>(nameIsPrefix ? mChannelPrefixConnections.get(channelName) : mChannelConnections.get(channelName), channelName, mOriginalThreadHandler);
	}


	SubscriptionDiskCache getSubscriptionDiskCache() {
		return mSubscriptionDiskCache;
	}


	<T> RapidCollectionReference<T> provideCollection(String collectionName, Class<T> itemClass) {
		if(!mCollectionConnections.containsKey(collectionName))
			mCollectionConnections.put(collectionName, new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, itemClass, mSubscriptionDiskCache, mDebugLogger));
		return new RapidCollectionReference<T>(mCollectionConnections.get(collectionName), collectionName, mOriginalThreadHandler, mJsonConverter);
	}


	RapidCollectionReference<Map<String, Object>> provideCollection(String collectionName) {
		if(!mCollectionConnections.containsKey(collectionName))
			mCollectionConnections.put(collectionName, new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, Map.class, mSubscriptionDiskCache, mDebugLogger));
		return new RapidCollectionReference<Map<String, Object>>(mCollectionConnections.get(collectionName), collectionName, mOriginalThreadHandler, mJsonConverter);
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
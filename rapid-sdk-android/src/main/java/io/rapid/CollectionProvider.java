package io.rapid;


import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.rapid.executor.RapidExecutor;


class CollectionProvider {
	private final RapidExecutor mExecutor;
	private final JsonConverterProvider mJsonConverter;
	private final SubscriptionDiskCache mSubscriptionDiskCache;
	private final RapidLogger mDebugLogger;
	private RapidConnection mConnection;
	@NonNull private Map<String, CollectionConnection> mCollectionConnections = new HashMap<>();
	@NonNull private Map<String, ChannelConnection> mChannelConnections = new HashMap<>();
	@NonNull private Map<String, ChannelConnection> mChannelPrefixConnections = new HashMap<>();


	CollectionProvider(RapidConnection connection, JsonConverterProvider jsonConverter, RapidExecutor executor, SubscriptionDiskCache subscriptionDiskCache, RapidLogger debugLogger) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mExecutor = executor;
		mSubscriptionDiskCache = subscriptionDiskCache;
		mDebugLogger = debugLogger;
	}


	@NonNull
	ChannelConnection findChannelBySubscriptionId(String subscriptionId) {
		for(String channelName : mChannelConnections.keySet()) {
			if(mChannelConnections.get(channelName).hasSubscription(subscriptionId))
				return mChannelConnections.get(channelName);
		}
		for(String channelName : mChannelPrefixConnections.keySet()) {
			if(mChannelPrefixConnections.get(channelName).hasSubscription(subscriptionId))
				return mChannelPrefixConnections.get(channelName);
		}
		throw new IllegalArgumentException("BaseCollectionSubscription not found");
	}


	@NonNull
	<T> RapidChannelPrefixReference<T> provideChannel(String channelName, Class<T> messageClass, boolean nameIsPrefix) {
		if(nameIsPrefix) {
			if(!mChannelPrefixConnections.containsKey(channelName))
				mChannelPrefixConnections.put(channelName, new WebSocketChannelConnection<>(mConnection, mJsonConverter, channelName, messageClass, mDebugLogger, true));
		} else {
			if(!mChannelConnections.containsKey(channelName))
				mChannelConnections.put(channelName, new WebSocketChannelConnection<>(mConnection, mJsonConverter, channelName, messageClass, mDebugLogger, false));
		}
		if(nameIsPrefix)
			return new RapidChannelPrefixReference<T>(mChannelPrefixConnections.get(channelName), channelName, mExecutor);
		else
			return new RapidChannelReference<T>(mChannelConnections.get(channelName), channelName, mExecutor);
	}


	SubscriptionDiskCache getSubscriptionDiskCache() {
		return mSubscriptionDiskCache;
	}


	@NonNull
	<T> RapidCollectionReference<T> provideCollection(String collectionName, Class<T> itemClass) {
		if(!mCollectionConnections.containsKey(collectionName))
			mCollectionConnections.put(collectionName, new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, itemClass, mSubscriptionDiskCache, mDebugLogger, mExecutor));
		return new RapidCollectionReference<T>(mCollectionConnections.get(collectionName), collectionName, mExecutor, mJsonConverter);
	}


	@NonNull
	RapidCollectionReference<Map<String, Object>> provideCollection(String collectionName) {
		if(!mCollectionConnections.containsKey(collectionName))
			mCollectionConnections.put(collectionName, new WebSocketCollectionConnection<>(mConnection, mJsonConverter, collectionName, Map.class, mSubscriptionDiskCache, mDebugLogger, mExecutor));
		return new RapidCollectionReference<Map<String, Object>>(mCollectionConnections.get(collectionName), collectionName, mExecutor, mJsonConverter);
	}


	CollectionConnection findCollectionByName(String collectionName) {
		return mCollectionConnections.get(collectionName);
	}


	@NonNull
	CollectionConnection findCollectionBySubscriptionId(String subscriptionId) {
		for(String channelName : mCollectionConnections.keySet()) {
			if(mCollectionConnections.get(channelName).hasSubscription(subscriptionId))
				return mCollectionConnections.get(channelName);
		}
		throw new IllegalArgumentException("BaseCollectionSubscription not found");
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
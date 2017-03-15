package io.rapid;


import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.rapid.converter.RapidJsonConverter;


class MockRapidCollectionImpl<T> implements RapidCollectionImpl<T> {
	private final RapidJsonConverter mJsonConverter;
	private final Class<T> mType;
	Map<String, String> mDb = new HashMap<>();
	Set<RapidSubscription<T>> mSubscriptions = new HashSet<>();


	public MockRapidCollectionImpl(Class<T> type, RapidJsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
		mType = type;
	}


	@Override
	public RapidFuture<T> add(T value) {
		RapidFuture<T> future = new RapidFuture<>();
		delayOperation(() -> {
			mDb.put(getNewKey(), toJson(value));
			future.invokeSuccess();
			notifyChange();
		});
		return future;
	}


	@Override
	public RapidFuture<T> edit(String key, T value) {
		RapidFuture<T> future = new RapidFuture<>();
		delayOperation(() -> {
			mDb.put(key, toJson(value));
			future.invokeSuccess();
			notifyChange();
		});
		return future;
	}


	@Override
	public RapidSubscription<T> subscribe(RapidObjectCallback<Collection<T>> callback) {
		RapidSubscription<T> subscription = new RapidSubscription<>(callback);
		mSubscriptions.add(subscription);
		subscription.setOnUnsubscribeCallback(() -> mSubscriptions.remove(subscription));
		return subscription;
	}


	private String toJson(T object) {
		try {
			return mJsonConverter.toJson(object);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	private T fromJson(String json) {
		try {
			return mJsonConverter.fromJson(json, mType);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	private void notifyChange() {
		for(RapidSubscription<T> subscription : mSubscriptions) {
			Collection<T> objects = new ArrayList<T>();
			for(String s : mDb.values()) {
				objects.add(fromJson(s));
			}
			subscription.invokeChange(objects);
		}
	}


	private String getNewKey() {
		return UUID.randomUUID().toString();
	}


	private void delayOperation(Runnable runnable) {
		new Handler().postDelayed(runnable, 200);
	}
}

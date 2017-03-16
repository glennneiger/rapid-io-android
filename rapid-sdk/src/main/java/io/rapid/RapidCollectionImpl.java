package io.rapid;


import java.util.Collection;


public interface RapidCollectionImpl<T> {
	RapidFuture<T> add(T value);
	RapidFuture<T> set(String key, T value);
	RapidSubscription subscribe(RapidCallback<Collection<T>> callback);
	RapidSubscription subscribeDocument(RapidCallback<T> callback);
}

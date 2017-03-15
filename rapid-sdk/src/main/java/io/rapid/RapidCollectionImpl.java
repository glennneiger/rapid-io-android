package io.rapid;


import java.util.Collection;


public interface RapidCollectionImpl<T> {
	RapidFuture<T> add(T value);
	RapidFuture<T> edit(String key, T value);
	RapidSubscription subscribe(RapidObjectCallback<Collection<T>> callback);
}

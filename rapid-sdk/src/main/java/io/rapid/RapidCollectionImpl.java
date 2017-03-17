package io.rapid;


public interface RapidCollectionImpl<T> {
	RapidFuture<T> add(T value);
	RapidFuture<T> set(String key, T value);
	RapidSubscription subscribe(RapidCollectionCallback<T> callback);
	RapidSubscription subscribeDocument(RapidDocumentCallback<T> callback);
}

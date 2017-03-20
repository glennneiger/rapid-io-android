package io.rapid;


public interface CollectionConnection<T> {
	RapidFuture<T> set(String key, T value);
	RapidSubscription subscribe(RapidCollectionCallback<T> callback);
	RapidSubscription subscribeDocument(RapidDocumentCallback<T> callback);
	void onValue(MessageVal valMessage);
	void onUpdate(MessageUpd updMessage);
}

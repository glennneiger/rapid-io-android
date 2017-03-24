package io.rapid;


public interface CollectionConnection<T> {
	RapidFuture<T> mutate(String id, T value);
	RapidSubscription subscribe(RapidCollectionCallback<T> callback, EntityOrder order, int limit, int skip, Filter filter);
	RapidSubscription subscribeDocument(RapidDocumentCallback<T> callback);
	void onValue(MessageVal valMessage);
	void onUpdate(MessageUpd updMessage);
	boolean isSubscribed();
	void resubscribe(EntityOrder order, int limit, int skip, Filter filter);
}

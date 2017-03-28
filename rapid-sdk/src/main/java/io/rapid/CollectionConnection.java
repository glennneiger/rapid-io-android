package io.rapid;


public interface CollectionConnection<T> {
	RapidFuture<T> mutate(String id, T value);
	RapidCollectionSubscription subscribe(RapidCollectionCallback<T> callback, EntityOrder order, int limit, int skip, Filter filter);
	RapidDocumentSubscription<T> subscribeDocument(String id, RapidDocumentCallback<T> callback);
	void onValue(MessageVal valMessage);
	void onUpdate(MessageUpd updMessage);
	boolean isSubscribed();
	void resubscribe(EntityOrder order, int limit, int skip, Filter filter);
}

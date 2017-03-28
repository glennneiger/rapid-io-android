package io.rapid;


public interface CollectionConnection<T> {
	RapidFuture<T> mutate(String id, T value);
	void subscribe(RapidCollectionSubscription<T> subscription);
	void subscribeDocument(RapidDocumentSubscription<T> subscription);
	void onValue(MessageVal valMessage);
	void onUpdate(MessageUpd updMessage);
	boolean hasActiveSubscription();
	void resubscribe();
}

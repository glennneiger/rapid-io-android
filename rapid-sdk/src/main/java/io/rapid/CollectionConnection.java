package io.rapid;


public interface CollectionConnection<T> {
	RapidFuture<T> mutate(String id, T value);
	void subscribe(RapidCollectionSubscription<T> subscription);
	void subscribeDocument(RapidDocumentSubscription<T> subscription);
	void onValue(String subscriptionId, String documents);
	void onUpdate(String subscriptionId, String document);
	boolean hasActiveSubscription();
	void resubscribe();
}

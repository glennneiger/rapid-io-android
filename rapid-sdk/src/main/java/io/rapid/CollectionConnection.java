package io.rapid;


interface CollectionConnection<T> {
	RapidFuture mutate(String id, T value);
	void subscribe(RapidCollectionSubscription<T> subscription);
	void subscribeDocument(RapidDocumentSubscription<T> subscription);
	void onValue(String subscriptionId, String documents);
	void onUpdate(String subscriptionId, String previousSiblingId, String document);
	boolean hasActiveSubscription();
	void resubscribe();
}

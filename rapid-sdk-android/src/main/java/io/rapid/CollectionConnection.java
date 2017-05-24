package io.rapid;


interface CollectionConnection<T> {
	RapidFuture mutate(String id, T value, Etag etag);
	void subscribe(Subscription<T> subscription);
	void onValue(String subscriptionId, String documents);
	void onFetchResult(String fetchId, String documentsJson);
	void onUpdate(String subscriptionId, String document);
	void onError(String subscriptionId, RapidError error);
	void onTimedOut();
	boolean hasActiveSubscription();
	void resubscribe();
	void onRemove(String subscriptionId, String document);
	void fetch(Subscription<T> subscription);
}

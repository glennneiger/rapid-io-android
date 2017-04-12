package io.rapid;


interface CollectionConnection<T> {
	RapidFuture mutate(String id, T value);
	void subscribe(Subscription<T> subscription);
	void onValue(String subscriptionId, String documents, boolean fromCache);
	void onUpdate(String subscriptionId, String previousSiblingId, String document);
	void onError(String subscriptionId, RapidError error);
	void onTimedOut();
	boolean hasActiveSubscription();
	void resubscribe();
}

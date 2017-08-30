package io.rapid;


import java.util.Map;


interface CollectionConnection<T> {
	RapidFuture mutate(String id, T value, RapidMutateOptions options);
	RapidFuture merge(String id, Map<String, Object> mergeMap, RapidMutateOptions options);
	void subscribe(BaseCollectionSubscription<T> subscription);
	void onValue(String subscriptionId, String documents);
	void onFetchResult(String fetchId, String documentsJson);
	void onUpdate(String subscriptionId, String document);
	void onError(String subscriptionId, RapidError error);
	void onTimedOut();
	boolean hasActiveSubscription();
	void resubscribe();
	void onRemove(String subscriptionId, String document);
	void fetch(BaseCollectionSubscription<T> subscription);
	RapidActionFuture onDisconnectMutate(String docId, T item, RapidMutateOptions options);
	RapidActionFuture onDisconnectMerge(String docId, Map<String, Object> mergeMap, RapidMutateOptions options);
	boolean hasSubscription(String subscriptionId);
}

package io.rapid;


import android.support.annotation.Nullable;

import java.util.Map;


class MockCollectionConnection<T> implements CollectionConnection<T> {

	@Nullable
	@Override
	public RapidFuture mutate(String id, T value, RapidMutateOptions options) {
		return null;
	}


	@Nullable
	@Override
	public RapidFuture merge(String id, Map<String, Object> mergeMap, RapidMutateOptions options) {
		return null;
	}


	@Override
	public void subscribe(BaseCollectionSubscription<T> subscription) {

	}


	@Override
	public void onValue(String subscriptionId, String documents) {

	}


	@Override
	public void onFetchResult(String fetchId, String documentsJson) {

	}


	@Override
	public void onUpdate(String subscriptionId, String document) {

	}


	@Override
	public void onError(String subscriptionId, RapidError error) {

	}


	@Override
	public void onTimedOut() {

	}


	@Override
	public boolean hasActiveSubscription() {
		return false;
	}


	@Override
	public void resubscribe() {

	}


	@Override
	public void onRemove(String subscriptionId, String document) {

	}


	@Override
	public void fetch(BaseCollectionSubscription<T> subscription) {

	}


	@Nullable
	@Override
	public RapidActionFuture onDisconnectMutate(String docId, T item, RapidMutateOptions options) {
		return null;
	}


	@Nullable
	@Override
	public RapidActionFuture onDisconnectMerge(String docId, Map<String, Object> mergeMap, RapidMutateOptions options) {
		return null;
	}
}

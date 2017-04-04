package io.rapid;


class MockCollectionConnection<T> implements CollectionConnection<T> {


	@Override
	public RapidFuture mutate(String id, T value) {
		return null;
	}


	@Override
	public void subscribe(RapidCollectionSubscription<T> subscription) {

	}


	@Override
	public void subscribeDocument(RapidDocumentSubscription<T> subscription) {

	}


	@Override
	public void onValue(String subscriptionId, String documents) {

	}


	@Override
	public void onUpdate(String subscriptionId, String document) {

	}


	@Override
	public boolean hasActiveSubscription() {
		return false;
	}


	@Override
	public void resubscribe() {

	}
}

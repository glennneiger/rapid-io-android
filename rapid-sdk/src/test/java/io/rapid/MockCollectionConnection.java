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
	public void onUpdate(String subscriptionId, String previousSiblingId, String document) {

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
}

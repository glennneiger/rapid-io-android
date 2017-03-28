package io.rapid;


class MockCollectionConnection<T> implements CollectionConnection<T> {

	@Override
	public RapidFuture<T> mutate(String id, T value) {
		return null;
	}


	@Override
	public void subscribe(RapidCollectionSubscription<T> subscription) {

	}


	@Override
	public void subscribeDocument(RapidDocumentSubscription<T> subscription) {

	}


	@Override
	public void onValue(MessageVal valMessage) {

	}


	@Override
	public void onUpdate(MessageUpd updMessage) {

	}


	@Override
	public boolean hasActiveSubscription() {
		return false;
	}


	@Override
	public void resubscribe() {

	}
}

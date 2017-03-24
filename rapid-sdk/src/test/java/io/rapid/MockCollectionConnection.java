package io.rapid;


class MockCollectionConnection<T> implements CollectionConnection<T> {
	@Override
	public RapidFuture<T> mutate(String id, T value) {
		return null;
	}


	@Override
	public RapidSubscription subscribe(RapidCollectionCallback<T> callback, EntityOrder order, int limit, int skip, Filter filter) {
		return null;
	}


	@Override
	public RapidSubscription subscribeDocument(RapidDocumentCallback<T> callback) {
		return null;
	}


	@Override
	public void onValue(MessageVal valMessage) {

	}


	@Override
	public void onUpdate(MessageUpd updMessage) {

	}


	@Override
	public boolean isSubscribed() {
		return false;
	}


	@Override
	public void resubscribe(EntityOrder order, int limit, int skip, Filter filter) {

	}
}

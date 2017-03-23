package io.rapid;


public class RapidCollectionReference<T> {

	private final String mCollectionName;
	private CollectionConnection<T> mConnection;
	private EntityOrder mOrder;


	public RapidCollectionReference(Rapid rapid, String collectionName, Class<T> type) {
		mCollectionName = collectionName;
		mConnection = new WebSocketCollectionConnection<>(rapid, collectionName, type);
	}


	// Query methods
	public RapidCollectionReference<T> equalTo(String property, String value) {
		return this;
	}


	public RapidCollectionReference<T> not(String property, String value) {
		return this;
	}


	public RapidCollectionReference<T> lessThan(String property, int value) {
		return this;
	}


	public RapidCollectionReference<T> greaterThan(String property, int value) {
		return this;
	}


	public RapidCollectionReference<T> greaterOrEqualThan(String property, int value) {
		return this;
	}


	public RapidCollectionReference<T> beginOr() {
		return this;
	}

	public RapidCollectionReference<T> beginAnd() {
		return this;
	}


	public RapidCollectionReference<T> endOr() {
		return this;
	}


	public RapidCollectionReference<T> endAnd() {
		return this;
	}


	public RapidCollectionReference<T> between(String property, int from, int to) {
		return this;
	}


	public RapidCollectionReference<T> limit(int limit) {
		return this;
	}


	public RapidCollectionReference<T> skip(int skip) {
		return this;
	}


	public RapidCollectionReference<T> first() {
		return limit(1);
	}


	public RapidCollectionReference<T> orderBy(String property, Sorting sorting) {
		if(mOrder == null) mOrder = new EntityOrder();
		mOrder.putOrder(property, sorting);

		return this;
	}


	public RapidCollectionReference<T> orderBy(String property) {
		return orderBy(property, Sorting.ASC);
	}


	public RapidSubscription subscribe(RapidCollectionCallback<T> callback) {
		return mConnection.subscribe(callback, mOrder);
	}


	public RapidDocumentReference<T> newDocument() {
		return new RapidDocumentReference<>(mCollectionName, mConnection);
	}


	public RapidDocumentReference<T> document(String documentId) {
		return new RapidDocumentReference<>(mCollectionName, mConnection, documentId);
	}


	boolean isSubscribed() {
		return mConnection.isSubscribed();
	}


	void onValue(MessageVal valMessage) {
		mConnection.onValue(valMessage);
	}


	void onUpdate(MessageUpd updMessage) {
		mConnection.onUpdate(updMessage);
	}
}

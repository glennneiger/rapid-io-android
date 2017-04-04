package io.rapid;


import android.os.Handler;


public class RapidCollectionReference<T> {

	private final String mCollectionName;
	private final Handler mUiThreadHandler;
	private RapidCollectionSubscription<T> mSubscription;
	private CollectionConnection<T> mConnection;


	RapidCollectionReference(CollectionConnection<T> collectionConnection, String collectionName, Handler uiThreadHandler) {
		mCollectionName = collectionName;
		mConnection = collectionConnection;
		mUiThreadHandler = uiThreadHandler;
		initSubscription();
	}


	public RapidCollectionReference<T> equalTo(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


	// Query methods


	public RapidCollectionReference<T> equalTo(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


	public RapidCollectionReference<T> equalTo(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


	public RapidCollectionReference<T> equalTo(String property, boolean value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.BooleanPropertyValue(value)));
		return this;
	}


	public RapidCollectionReference<T> notEqualTo(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value)));
		return this;
	}


	public RapidCollectionReference<T> notEqualTo(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value)));
		return this;
	}


	public RapidCollectionReference<T> notEqualTo(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value)));
		return this;
	}


	public RapidCollectionReference<T> lessThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> lessThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> lessOrEqualThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> lessOrEqualThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> greaterThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> greaterThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> greaterOrEqualThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> greaterOrEqualThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> between(String property, int from, int to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	public RapidCollectionReference<T> between(String property, double from, double to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	public RapidCollectionReference<T> idEqualTo(String id) {
		equalTo(Config.ID_IDENTIFIER, id);
		return this;
	}


	public RapidCollectionReference<T> idNotEqualTo(String id) {
		notEqualTo(Config.ID_IDENTIFIER, id);
		return this;
	}


	public RapidCollectionReference<T> beginOr() {
		Filter.Or or = new Filter.Or();
		mSubscription.getFilterStack().peek().add(or);
		mSubscription.getFilterStack().push(or);
		return this;
	}


	// Groups


	public RapidCollectionReference<T> beginAnd() {
		Filter.And and = new Filter.And();
		mSubscription.getFilterStack().peek().add(and);
		mSubscription.getFilterStack().push(and);
		return this;
	}


	public RapidCollectionReference<T> beginNot() {
		Filter.Not not = new Filter.Not();
		mSubscription.getFilterStack().peek().add(not);
		mSubscription.getFilterStack().push(not);
		return this;
	}


	public RapidCollectionReference<T> endOr() {
		if(!(mSubscription.getFilterStack().peek() instanceof Filter.Or))
			throw new IllegalArgumentException("Trying to end OR group inside another group.");

		mSubscription.getFilterStack().pop();
		return this;
	}


	public RapidCollectionReference<T> endAnd() {
		if(!(mSubscription.getFilterStack().peek() instanceof Filter.And))
			throw new IllegalArgumentException("Trying to end AND group inside another group.");

		mSubscription.getFilterStack().pop();
		return this;
	}


	public RapidCollectionReference<T> endNot() {
		if(!(mSubscription.getFilterStack().peek() instanceof Filter.Not))
			throw new IllegalArgumentException("Trying to end NOT group inside another group.");

		mSubscription.getFilterStack().pop();
		return this;
	}


	public RapidCollectionReference<T> orderBy(String property, Sorting sorting) {
		mSubscription.orderBy(property, sorting);

		return this;
	}


	// Order


	public RapidCollectionReference<T> orderBy(String property) {
		return orderBy(property, Sorting.ASC);
	}


	public RapidCollectionReference<T> limit(int limit) {
		mSubscription.setLimit(limit);
		return this;
	}


	// Limit, Skip


	public RapidCollectionReference<T> skip(int skip) {
		mSubscription.setSkip(skip);
		return this;
	}


	public RapidCollectionReference<T> first() {
		return limit(1);
	}


	public RapidCollectionSubscription subscribe(RapidCollectionCallback<T> callback) {
		mSubscription.setCallback(callback);
		mConnection.subscribe(mSubscription);

		RapidCollectionSubscription<T> temp = mSubscription;
		initSubscription();
		return temp;
	}


	// Operations


	public RapidDocumentReference<T> newDocument() {
		return new RapidDocumentReference<>(mUiThreadHandler, mCollectionName, mConnection);
	}


	public RapidDocumentReference<T> document(String documentId) {
		return new RapidDocumentReference<>(mUiThreadHandler, mCollectionName, mConnection, documentId);
	}


	void resubscribe() {
		mConnection.resubscribe();
	}


	// Private


	boolean isSubscribed() {
		return mConnection.hasActiveSubscription();
	}


	void onValue(String subscriptionId, String documents) {
		mConnection.onValue(subscriptionId, documents);
	}


	void onUpdate(String subscriptionId, String documents) {
		mConnection.onUpdate(subscriptionId, documents);
	}


	private void initSubscription() {
		mSubscription = new RapidCollectionSubscription<T>(mCollectionName, mUiThreadHandler);
		mSubscription.getFilterStack().push(new Filter.And());
	}
}

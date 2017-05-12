package io.rapid;


import android.os.Handler;

import java.util.Date;

import io.rapid.converter.RapidJsonConverter;


@SuppressWarnings("WeakerAccess")
public class RapidCollectionReference<T> {

	private final String mCollectionName;
	private final Handler mUiThreadHandler;
	private final RapidJsonConverter mJsonConverter;
	private RapidCollectionSubscription<T> mSubscription;
	private CollectionConnection<T> mConnection;


	RapidCollectionReference(CollectionConnection<T> collectionConnection, String collectionName, Handler uiThreadHandler, RapidJsonConverter jsonConverter) {
		mCollectionName = collectionName;
		mConnection = collectionConnection;
		mUiThreadHandler = uiThreadHandler;
		mJsonConverter = jsonConverter;
		initSubscription();
	}


	// Query methods


	public RapidCollectionReference<T> equalTo(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


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


	public RapidCollectionReference<T> equalTo(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value, mJsonConverter)));
		return this;
	}


	//---


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


	public RapidCollectionReference<T> notEqualTo(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value, mJsonConverter)));
		return this;
	}


	//---


	public RapidCollectionReference<T> lessThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
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


	public RapidCollectionReference<T> lessThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	public RapidCollectionReference<T> lessOrEqualThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
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


	public RapidCollectionReference<T> lessOrEqualThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	public RapidCollectionReference<T> greaterThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
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


	public RapidCollectionReference<T> greaterThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	public RapidCollectionReference<T> greaterOrEqualThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
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


	public RapidCollectionReference<T> greaterOrEqualThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	public RapidCollectionReference<T> between(String property, String from, String to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
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


	public RapidCollectionReference<T> between(String property, Date from, Date to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	//---


	public RapidCollectionReference<T> idEqualTo(String id) {
		equalTo(Config.ID_IDENTIFIER, id);
		return this;
	}


	public RapidCollectionReference<T> idNotEqualTo(String id) {
		notEqualTo(Config.ID_IDENTIFIER, id);
		return this;
	}


	// String operations


	public RapidCollectionReference<T> contains(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_CONTAINS, value)));
		return this;
	}


	public RapidCollectionReference<T> startsWith(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_STARTS_WITH, value)));
		return this;
	}


	public RapidCollectionReference<T> endsWith(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_ENDS_WITH, value)));
		return this;
	}

	// Array contains


	public RapidCollectionReference<?> arrayContains(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_ARRAY_CONTAINS, value)));
		return this;
	}


	public RapidCollectionReference<?> arrayContains(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_ARRAY_CONTAINS, value)));
		return this;
	}


	public RapidCollectionReference<?> arrayContains(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_ARRAY_CONTAINS, value)));
		return this;
	}


	// Groups


	public RapidCollectionReference<T> beginOr() {
		Filter.Or or = new Filter.Or();
		mSubscription.getFilterStack().peek().add(or);
		mSubscription.getFilterStack().push(or);
		return this;
	}


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


	// Order


	public RapidCollectionReference<T> orderBy(String property, Sorting sorting) {
		mSubscription.orderBy(property, sorting);

		return this;
	}


	public RapidCollectionReference<T> orderBy(String property) {
		return orderBy(property, Sorting.ASC);
	}


	public RapidCollectionReference<T> orderByDocumentId() {
		return orderBy(Config.ID_IDENTIFIER, Sorting.ASC);
	}


	public RapidCollectionReference<T> orderByDocumentId(Sorting sorting) {
		return orderBy(Config.ID_IDENTIFIER, sorting);
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


	// Operations


	public RapidDocumentReference<T> newDocument() {
		return new RapidDocumentReference<>(mUiThreadHandler, mCollectionName, mConnection);
	}


	public RapidDocumentReference<T> document(String documentId) {
		return new RapidDocumentReference<>(mUiThreadHandler, mCollectionName, mConnection, documentId);
	}


	public RapidCollectionSubscription subscribe(RapidCallback.Collection<T> callback) {
		return subscribeWithListUpdates((rapidDocuments, listUpdates) -> callback.onValueChanged(rapidDocuments));
	}


	public RapidCollectionSubscription subscribeWithListUpdates(RapidCallback.CollectionUpdates<T> callback) {
		mSubscription.setCallback(callback);
		mConnection.subscribe(mSubscription);

		RapidCollectionSubscription<T> temp = mSubscription;
		initSubscription();
		return temp;
	}


	public <S> RapidCollectionMapReference<T, S> map(RapidCollectionMapReference.MapFunction<T, S> mapFunction) {
		return new RapidCollectionMapReference<>(this, mapFunction);
	}


	public void onRemove(String subscriptionId, String documentJson) {
		mConnection.onRemove(subscriptionId, documentJson);
	}


	// Private


	void resubscribe() {
		mConnection.resubscribe();
	}


	CollectionConnection<T> getConnection() {
		return mConnection;
	}


	RapidCollectionSubscription<T> getSubscription() {
		return mSubscription;
	}


	boolean isSubscribed() {
		return mConnection.hasActiveSubscription();
	}


	void onValue(String subscriptionId, String documents) {
		mConnection.onValue(subscriptionId, documents);
	}


	void onUpdate(String subscriptionId, String documents) {
		mConnection.onUpdate(subscriptionId, documents);
	}


	void onError(String subscriptionId, RapidError error) {
		mConnection.onError(subscriptionId, error);
	}


	void onTimedOut() {
		mConnection.onTimedOut();
	}


	void initSubscription() {
		mSubscription = new RapidCollectionSubscription<>(mCollectionName, mUiThreadHandler);
	}


}

package io.rapid;


import java.util.Stack;


public class RapidCollectionReference<T> {

	private final String mCollectionName;
	private CollectionConnection<T> mConnection;
	private int mLimit = Config.DEFAULT_LIMIT;
	private int mSkip = 0;
	private EntityOrder mOrder;
	private Stack<FilterGroup> mFilterStack = new Stack<>();


	public RapidCollectionReference(CollectionConnection<T> collectionConnection, String collectionName) {
		mCollectionName = collectionName;
		mConnection = collectionConnection;
		mFilterStack.push(new FilterAnd());
	}


	// Query methods
	public RapidCollectionReference<T> equalTo(String property, String value) {
		mFilterStack.peek().add(new FilterValue(property, new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUALS, value)));
		return this;
	}


	public RapidCollectionReference<T> lessThan(String property, int value) {
		mFilterStack.peek().add(new FilterValue(property, new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> lessOrEqualThan(String property, int value) {
		mFilterStack.peek().add(new FilterValue(property, new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> greaterThan(String property, int value) {
		mFilterStack.peek().add(new FilterValue(property, new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> greaterOrEqualThan(String property, int value) {
		mFilterStack.peek().add(new FilterValue(property, new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
		return this;
	}


	public RapidCollectionReference<T> beginOr() {
		FilterOr or = new FilterOr();
		mFilterStack.peek().add(or);
		mFilterStack.push(or);
		return this;
	}

	public RapidCollectionReference<T> beginAnd() {
		FilterAnd and = new FilterAnd();
		mFilterStack.peek().add(and);
		mFilterStack.push(and);
		return this;
	}


	public RapidCollectionReference<T> endOr() {
		mFilterStack.pop();
		return this;
	}


	public RapidCollectionReference<T> endAnd() {
		mFilterStack.pop();
		return this;
	}


	public RapidCollectionReference<T> between(String property, int from, int to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	public RapidCollectionReference<T> limit(int limit) {
		mLimit = limit;
		return this;
	}


	public RapidCollectionReference<T> skip(int skip) {
		mSkip = skip;
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
		return mConnection.subscribe(callback, getOrder(), getLimit(), getSkip(), getFilter());
	}


	public RapidDocumentReference<T> newDocument() {
		return new RapidDocumentReference<>(mCollectionName, mConnection);
	}


	public RapidDocumentReference<T> document(String documentId) {
		return new RapidDocumentReference<>(mCollectionName, mConnection, documentId);
	}


	Filter getFilter() {
		if (mFilterStack.size()!=1){
			throw new IllegalArgumentException("Wrong filter structure");
		}
		return mFilterStack.peek();
	}


	EntityOrder getOrder() {
		return mOrder;
	}


	int getLimit() {
		return mLimit;
	}


	int getSkip() {
		return mSkip;
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

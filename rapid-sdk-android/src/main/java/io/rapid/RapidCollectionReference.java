package io.rapid;


import android.arch.lifecycle.LiveData;
import android.os.Handler;

import java.util.Date;
import java.util.List;

import io.rapid.lifecycle.RapidLiveData;


/**
 * Rapid.io collection reference
 * <p>
 * Provides methods for querying data and mutating data
 * <p>
 * This class is supposed to be used as a fluent interface
 * Example:
 * <p>
 * <pre>
 * {@code
 * Rapid.getInstance().collection("messages", Message.class)
 * 		.equalTo("receiver", "carl01")
 * 		.beginOr()
 * 			.beginAnd()
 * 				.equalTo("sender", "john123")
 * 				.equalTo("sender", "john123")
 * 			.endAnd()
 * 			.equalTo("sender", "john123")
 * 			.greaterOrEqualThan("urgency", 1)
 * 		.endOr()
 * 		.orderBy("sentDate", Sorting.DESC)
 * 		.orderBy("urgency", Sorting.ASC)
 * 		.limit(50)
 * 		.skip(10)
 * 		.subscribe(documents -> {
 * 			log(documents.toString());
 * 		})
 * 		.onError(error -> {
 * 			error.printStackTrace();
 * 		});
 * }
 * </pre>
 *
 * @param <T> type of the items in collection
 */
@SuppressWarnings("WeakerAccess")
public class RapidCollectionReference<T> {

	private final String mCollectionName;
	private final Handler mUiThreadHandler;
	private final JsonConverterProvider mJsonConverter;
	private RapidCollectionSubscription<T> mSubscription;
	private CollectionConnection<T> mConnection;


	RapidCollectionReference(CollectionConnection<T> collectionConnection, String collectionName, Handler uiThreadHandler, JsonConverterProvider jsonConverter) {
		mCollectionName = collectionName;
		mConnection = collectionConnection;
		mUiThreadHandler = uiThreadHandler;
		mJsonConverter = jsonConverter;
		mSubscription = new RapidCollectionSubscription<>(mCollectionName, mUiThreadHandler);
	}


	// Query methods


	/**
	 * Query method saying that specified property should be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> equalTo(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> equalTo(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> equalTo(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> equalTo(String property, boolean value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.BooleanPropertyValue(value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> equalTo(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, value, mJsonConverter)));
		return this;
	}


	//---


	/**
	 * Query method saying that specified property should not be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> notEqualTo(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should not be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> notEqualTo(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should not be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> notEqualTo(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should not be equal to desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> notEqualTo(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_NOT_EQUAL, value, mJsonConverter)));
		return this;
	}


	//---


	/**
	 * Query method saying that specified property should be less than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be less than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be less than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be less than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	/**
	 * Query method saying that specified property should be less or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessOrEqualThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be less or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessOrEqualThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be less or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessOrEqualThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be less or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> lessOrEqualThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_LESS_OR_EQUAL_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	/**
	 * Query method saying that specified property should be greater than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be greater than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be greater than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be greater than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	/**
	 * Query method saying that specified property should be greater or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterOrEqualThan(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be greater or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterOrEqualThan(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be greater or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterOrEqualThan(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value)));
		return this;
	}


	/**
	 * Query method saying that specified property should be greater or equal than desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> greaterOrEqualThan(String property, Date value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DatePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_OR_EQUAL_THAN, value, mJsonConverter)));
		return this;
	}


	//---


	/**
	 * Query method saying that specified property should be between desired value (bounds inclusive)
	 *
	 * @param property property name
	 * @param from     left bound for value
	 * @param to       right bound for value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> between(String property, String from, String to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	/**
	 * Query method saying that specified property should be between desired value (bounds inclusive)
	 *
	 * @param property property name
	 * @param from     left bound for value
	 * @param to       right bound for value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> between(String property, int from, int to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	/**
	 * Query method saying that specified property should be between desired value (bounds inclusive)
	 *
	 * @param property property name
	 * @param from     left bound for value
	 * @param to       right bound for value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> between(String property, double from, double to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	/**
	 * Query method saying that specified property should be between desired value (bounds inclusive)
	 *
	 * @param property property name
	 * @param from     left bound for value
	 * @param to       right bound for value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> between(String property, Date from, Date to) {
		beginAnd();
		greaterOrEqualThan(property, from);
		lessOrEqualThan(property, to);
		endAnd();
		return this;
	}


	//---


	/**
	 * Query method saying that document ID should be equal to desired value
	 *
	 * @param id desired ID value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> idEqualTo(String id) {

		equalTo(Config.ID_IDENTIFIER, id);
		return this;
	}


	/**
	 * Query method saying that document ID should not be equal to desired value
	 *
	 * @param id desired ID value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> idNotEqualTo(String id) {
		notEqualTo(Config.ID_IDENTIFIER, id);
		return this;
	}


	// String operations


	/**
	 * Query method saying that specified String property should contain desired String value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> contains(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_CONTAINS, value)));
		return this;
	}


	/**
	 * Query method saying that specified String property should start with desired String value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> startsWith(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_STARTS_WITH, value)));
		return this;
	}


	/**
	 * Query method saying that specified String property should end with desired String value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> endsWith(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_ENDS_WITH, value)));
		return this;
	}


	// Array contains


	/**
	 * Query method saying that specified array property should contain desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<?> arrayContains(String property, String value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_ARRAY_CONTAINS, value)));
		return this;
	}


	/**
	 * Query method saying that specified array property should contain desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<?> arrayContains(String property, int value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_ARRAY_CONTAINS, value)));
		return this;
	}


	/**
	 * Query method saying that specified array property should contain desired value
	 *
	 * @param property property name
	 * @param value    desired value
	 * @return collection reference itself
	 */
	public RapidCollectionReference<?> arrayContains(String property, double value) {
		mSubscription.getFilterStack().peek().add(new FilterValue(property, new FilterValue.DoublePropertyValue(FilterValue.PropertyValue.TYPE_ARRAY_CONTAINS, value)));
		return this;
	}


	// Groups


	/**
	 * Begin logical OR group for querying collection
	 * <p>
	 * This group must be ended by corresponding {@link #endOr()} call. Otherwise {@link RuntimeException} will be thrown
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> beginOr() {
		Filter.Or or = new Filter.Or();
		mSubscription.getFilterStack().peek().add(or);
		mSubscription.getFilterStack().push(or);
		return this;
	}


	/**
	 * Begin logical AND group for querying collection
	 * <p>
	 * This group must be ended by corresponding {@link #endAnd()} call. Otherwise {@link RuntimeException} will be thrown
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> beginAnd() {
		Filter.And and = new Filter.And();
		mSubscription.getFilterStack().peek().add(and);
		mSubscription.getFilterStack().push(and);
		return this;
	}


	/**
	 * Begin logical NOT group for querying collection
	 * <p>
	 * This group must be ended by corresponding {@link #endNot()} call. Otherwise {@link RuntimeException} will be thrown
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> beginNot() {
		Filter.Not not = new Filter.Not();
		mSubscription.getFilterStack().peek().add(not);
		mSubscription.getFilterStack().push(not);
		return this;
	}


	/**
	 * End logical OR group for querying collection
	 * <p>
	 * There must be an open OR group (using {@link #beginOr()} call). Otherwise {@link RuntimeException} will be thrown
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> endOr() {
		if(!(mSubscription.getFilterStack().peek() instanceof Filter.Or))
			throw new IllegalArgumentException("Trying to end OR group inside another group.");

		mSubscription.getFilterStack().pop();
		return this;
	}


	/**
	 * End logical AND group for querying collection
	 * <p>
	 * There must be an open AND group (using {@link #beginAnd()} call). Otherwise {@link RuntimeException} will be thrown
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> endAnd() {
		if(!(mSubscription.getFilterStack().peek() instanceof Filter.And))
			throw new IllegalArgumentException("Trying to end AND group inside another group.");

		mSubscription.getFilterStack().pop();
		return this;
	}


	/**
	 * End logical NOT group for querying collection
	 * <p>
	 * There must be an open NOT group (using {@link #beginNot()} call). Otherwise {@link RuntimeException} will be thrown
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> endNot() {
		if(!(mSubscription.getFilterStack().peek() instanceof Filter.Not))
			throw new IllegalArgumentException("Trying to end NOT group inside another group.");

		mSubscription.getFilterStack().pop();
		return this;
	}


	// Order


	/**
	 * Order collection by desired property
	 * <p>
	 * Use `sorting` param for specifying direction of order - either {@link Sorting#ASC} or {@link Sorting#DESC}
	 *
	 * @param property desired property
	 * @param sorting  sorting direction
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> orderBy(String property, Sorting sorting) {
		mSubscription.orderBy(property, sorting);

		return this;
	}


	/**
	 * Order collection by desired property using ascending direction
	 * <p>
	 * Use {@link #orderBy(String, Sorting)} to specify order direction
	 *
	 * @param property desired property
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> orderBy(String property) {
		return orderBy(property, Sorting.ASC);
	}


	/**
	 * Order collection by document ID using ascending direction
	 * <p>
	 * Use {@link #orderByDocumentId(Sorting)} to specify order direction
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> orderByDocumentId() {
		return orderBy(Config.ID_IDENTIFIER, Sorting.ASC);
	}


	/**
	 * Order collection by document ID
	 * <p>
	 * Use `sorting` param for specifying direction of order - either {@link Sorting#ASC} or {@link Sorting#DESC}
	 *
	 * @param sorting sorting direction
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> orderByDocumentId(Sorting sorting) {
		return orderBy(Config.ID_IDENTIFIER, sorting);
	}


	// Limit, Skip


	/**
	 * Limit results to specified number of documents (from top)
	 * <p>
	 * You can use this together with {@link #skip(int)} to create paging mechanism
	 *
	 * @param limit upper limit for number of returned documents (max. 250)
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> limit(int limit) {
		mSubscription.setLimit(limit);
		return this;
	}


	/**
	 * Skip specific number of documents (from top)
	 * <p>
	 * You can use this together with {@link #limit(int)} to create paging mechanism
	 *
	 * @param skip number of skipped documents
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> skip(int skip) {
		mSubscription.setSkip(skip);
		return this;
	}


	/**
	 * Get only first document in specified order
	 *
	 * @return collection reference itself
	 */
	public RapidCollectionReference<T> first() {
		return limit(1);
	}


	// Operations


	/**
	 * Provides new document reference. Essentially does the same as {@link #document(String)} but a new document ID is generated.
	 * <p>
	 * Document reference can be used to mutate the document or subscribe to it
	 *
	 * @return document reference
	 */
	public RapidDocumentReference<T> newDocument() {
		return new RapidDocumentReference<>(mUiThreadHandler, mCollectionName, mConnection);
	}


	/**
	 * Provides document reference for a document with specified ID
	 * <p>
	 * Document reference can be used to mutate the document or subscribe to it
	 *
	 * @param documentId document ID
	 * @return
	 */
	public RapidDocumentReference<T> document(String documentId) {
		return new RapidDocumentReference<>(mUiThreadHandler, mCollectionName, mConnection, documentId);
	}


	/**
	 * Subscribes to a collection using query built using fluent interface before calling this method
	 * <p>
	 * Use {@link #subscribeWithListUpdates(RapidCallback.CollectionUpdates)} to receive list update metadata (list refresh, item moved, item removed, item added)
	 *
	 * @param callback callback function to receive collection updates as list of documents
	 * @return subscription with ability to unsubscribe, add error listener, etc.
	 */
	public RapidCollectionSubscription subscribe(RapidCallback.Collection<T> callback) {
		return subscribeWithListUpdates((rapidDocuments, listUpdates) -> callback.onValueChanged(rapidDocuments));
	}


	/**
	 * Subscribes to a collection using query built using fluent interface before calling this method
	 * <p>
	 * Together with list of documents callback will receive list update metadata (list refresh, item moved, item removed, item added)
	 * <p>
	 * See {@link ListUpdate}
	 *
	 * @param callback callback function to receive collection updates as list of documents and list update metadata
	 * @return subscription with ability to unsubscribe, add error listener, etc.
	 */
	public RapidCollectionSubscription subscribeWithListUpdates(RapidCallback.CollectionUpdates<T> callback) {
		mSubscription.setCallback(callback);
		mConnection.subscribe(mSubscription);

		return mSubscription;
	}


	/**
	 * Fetch one-time collection snapshot using query built using fluent interface before calling this method
	 *
	 * @param callback callback function to receive collection updates as list of documents
	 */
	public RapidCollectionSubscription<T> fetch(RapidCallback.Collection<T> callback) {
		mSubscription.setCallback((rapidDocuments, listUpdates) -> callback.onValueChanged(rapidDocuments));
		mConnection.fetch(mSubscription);

		return mSubscription;
	}


	/**
	 * Convenience method for manipulating data before they are received within subscribe callback.
	 *
	 * @param mapFunction function that will transform every single document coming to subscribe callback
	 * @return collection reference itself
	 */
	public <S> RapidCollectionMapReference<T, S> map(RapidCollectionMapReference.MapFunction<T, S> mapFunction) {
		return new RapidCollectionMapReference<>(this, mapFunction);
	}


	public LiveData<List<RapidDocument<T>>> getLiveData() {
		return new RapidLiveData<T>(this);
	}


	// Private


	CollectionConnection<T> getConnection() {
		return mConnection;
	}


	RapidCollectionSubscription<T> getSubscription() {
		return mSubscription;
	}


	void onError(String subscriptionId, RapidError error) {
		mConnection.onError(subscriptionId, error);
	}


}

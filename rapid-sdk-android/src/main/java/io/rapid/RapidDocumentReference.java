package io.rapid;


import android.support.annotation.NonNull;

import java.util.Map;

import io.rapid.executor.RapidExecutor;


/**
 * Rapid.io document reference
 * <p>
 * Provides methods for querying data and mutating data
 *
 * @param <T> type of the items in collection
 */
@SuppressWarnings("WeakerAccess")
public class RapidDocumentReference<T> {
	private final CollectionConnection<T> mImpl;
	private final String mId;
	@NonNull private final RapidDocumentSubscription<T> mSubscription;
	private RapidExecutor mExecutor;
	private AuthHelper mAuth;


	RapidDocumentReference(RapidExecutor executor, String collectionName, CollectionConnection<T> impl, AuthHelper auth) {
		this(executor, collectionName, impl, IdProvider.getNewDocumentId(), auth);
	}


	RapidDocumentReference(RapidExecutor executor, String collectionName, CollectionConnection<T> impl, String documentId, AuthHelper auth) {
		mExecutor = executor;
		mId = documentId;
		mImpl = impl;
		mSubscription = new RapidDocumentSubscription<>(documentId, collectionName, executor);
		mAuth = auth;
	}


	public CollectionConnection<T> getConnection() {
		return mImpl;
	}


	@NonNull
	RapidDocumentSubscription<T> getSubscription() {
		return mSubscription;
	}


	/**
	 * Returns unique document ID
	 *
	 * @return document ID
	 */
	public String getId() {
		return mId;
	}


	/**
	 * Mutate document (set new value)
	 *
	 * @param item new content for the document
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidFuture mutate(T item) {
		return mutate(item, null);
	}


	/**
	 * Mutate document (set new value) with options
	 *
	 * @param item    new content for the document
	 * @param options options allowing to expect specific Etag value or autofilling properties with server values
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidFuture mutate(T item, RapidMutateOptions options) {
		return mImpl.mutate(mId, item, options);
	}


	/**
	 * Merge map into document (set new value). This will add/replace properties to the document. It woll not replace the entire body of the doc with the map.
	 *
	 * @param mergeMap map to merge to the doc
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidFuture merge(Map<String, Object> mergeMap) {
		return merge(mergeMap, null);
	}


	/**
	 * Merge map into document (set new value) with options. This will add/replace properties to the document. It woll not replace the entire body of the doc with the map.
	 *
	 * @param mergeMap map to merge to the doc
	 * @param options  options allowing to expect specific Etag value or autofilling properties with server values
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidFuture merge(Map<String, Object> mergeMap, RapidMutateOptions options) {
		return mImpl.merge(mId, mergeMap, options);
	}


	@NonNull
	public RapidDocumentOnDisconnectReference<T> onDisconnect() {
		return new RapidDocumentOnDisconnectReference<>(mImpl, mId);
	}


	/**
	 * Concurrency-safe mutate/delete document (set new value)
	 * <p>
	 * When you provide document transformer, this operation will safely update document value,
	 * because within documentExecutor you can be sure you are updating the latest version available on backend.
	 * <p>
	 * The SDK will automatically try to update the document until there is no conflicting version on backend so the document
	 * transformer function may be called multiple times.
	 *
	 * @param documentExecutor function responsible for updating the latest version of document available on backend
	 *                         you need to return one of `RapidDocumentExecutor.mutate(value)`, `RapidDocumentExecutor.delete()` or `RapidDocumentExecutor.cancel()`
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	@NonNull
	public RapidFuture execute(@NonNull RapidDocumentExecutor.Callback<T> documentExecutor) {
		RapidFuture result = new RapidFuture(mExecutor);
		fetch(document -> {
					RapidDocumentExecutor.Result<T> executorResult = documentExecutor.execute(document);
					if(executorResult.getType() == RapidDocumentExecutor.Result.TYPE_CANCEL) {
						result.invokeSuccess();
					} else if(executorResult.getType() == RapidDocumentExecutor.Result.TYPE_MUTATE) {
						RapidMutateOptions options = executorResult.getOptions();
						if(options == null)
							options = new RapidMutateOptions.Builder().build();

						options.setExpectedEtag(document != null ? document.getEtag() : Etag.NO_ETAG);

						mutate(executorResult.getValue(), options).onError(error -> {
							if(error.getType() == RapidError.ErrorType.ETAG_CONFLICT) {
								execute(documentExecutor)
										.onSuccess(result::invokeSuccess)
										.onError(result::invokeError);
							} else {
								result.invokeError(error);
							}
						}).onSuccess(result::invokeSuccess);
					} else if(executorResult.getType() == RapidDocumentExecutor.Result.TYPE_MERGE) {
						RapidMutateOptions options = executorResult.getOptions();
						if(options == null)
							options = new RapidMutateOptions.Builder().build();

						options.setExpectedEtag(document != null ? document.getEtag() : Etag.NO_ETAG);

						merge((Map<String, Object>)executorResult.getValue(), options).onError(error -> {
							if(error.getType() == RapidError.ErrorType.ETAG_CONFLICT) {
								execute(documentExecutor)
										.onSuccess(result::invokeSuccess)
										.onError(result::invokeError);
							} else {
								result.invokeError(error);
							}
						}).onSuccess(result::invokeSuccess);
					}
				}
		);
		return result;
	}


	/**
	 * Delete the document from collection
	 *
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidFuture delete() {
		return delete(null);
	}


	/**
	 * Delete document (set new value) with options
	 *
	 * @param options options allowing to expect specific Etag value or autofilling properties with server values
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidFuture delete(RapidMutateOptions options) {
		return mImpl.mutate(mId, null, options);
	}


	/**
	 * Subscribe to single document updates
	 *
	 * @param callback callback function providing updated document on Main thread
	 */
	@NonNull
	public RapidDocumentSubscription<T> subscribe(RapidCallback.Document<T> callback) {
		if(mSubscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		mSubscription.setCallback(callback);
		mSubscription.setAuthToken(mAuth.getAuthToken());
		mImpl.subscribe(mSubscription);
		return mSubscription;
	}


	/**
	 * One-time fetch document value
	 *
	 * @param callback callback function providing document value on Main thread
	 */
	@NonNull
	public RapidDocumentSubscription<T> fetch(RapidCallback.Document<T> callback) {
		if(mSubscription.isSubscribed())
			throw new IllegalStateException("There is already a subscription subscribed to this reference. Unsubscribe it first.");
		mSubscription.setCallback(callback);
		mImpl.fetch(mSubscription);
		return mSubscription;
	}


	/**
	 * Convenience method for manipulating data before they are received within subscribe callback.
	 *
	 * @param mapFunction function that will transform document coming to subscribe callback
	 * @return document reference itself
	 */
	@NonNull
	public <S> RapidDocumentMapReference<T, S> map(RapidDocumentMapReference.MapFunction<T, S> mapFunction, AuthHelper auth) {
		return new RapidDocumentMapReference<>(this, mapFunction, auth);
	}

}

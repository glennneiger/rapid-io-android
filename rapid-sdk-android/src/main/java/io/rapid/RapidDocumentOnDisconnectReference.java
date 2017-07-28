package io.rapid;


import java.util.Map;


public class RapidDocumentOnDisconnectReference<T> {


	private final CollectionConnection<T> mCollectionConnection;
	private final String mDocId;


	public RapidDocumentOnDisconnectReference(CollectionConnection<T> collectionConnection, String docId) {
		mCollectionConnection = collectionConnection;
		mDocId = docId;
	}


	/**
	 * Mutate document (set new value)
	 *
	 * @param item new content for the document
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidActionFuture mutate(T item) {
		return mutate(item, null);
	}


	/**
	 * Mutate document (set new value) with options
	 *
	 * @param item    new content for the document
	 * @param options options allowing to expect specific Etag value or autofilling properties with server values
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidActionFuture mutate(T item, RapidMutateOptions options) {
		return mCollectionConnection.onDisconnectMutate(mDocId, item, options);
	}


	/**
	 * Merge map into document (set new value). This will add/replace properties to the document. It woll not replace the entire body of the doc with the map.
	 *
	 * @param mergeMap map to merge to the doc
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidActionFuture merge(Map<String, Object> mergeMap) {
		return merge(mergeMap, null);
	}


	/**
	 * Merge map into document (set new value) with options. This will add/replace properties to the document. It woll not replace the entire body of the doc with the map.
	 *
	 * @param mergeMap map to merge to the doc
	 * @param options  options allowing to expect specific Etag value or autofilling properties with server values
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidActionFuture merge(Map<String, Object> mergeMap, RapidMutateOptions options) {
		return mCollectionConnection.onDisconnectMerge(mDocId, mergeMap, options);
	}


	/**
	 * Delete the document from collection
	 *
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidActionFuture delete() {
		return delete(null);
	}


	/**
	 * Delete document (set new value) with options
	 *
	 * @param options options allowing to expect specific Etag value or autofilling properties with server values
	 * @return RapidFuture providing callbacks for onComplete, onCollectionError, onSuccess events
	 */
	public RapidActionFuture delete(RapidMutateOptions options) {
		return mutate(null, options);
	}

}

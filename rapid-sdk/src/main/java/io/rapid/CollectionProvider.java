package io.rapid;


import java.util.HashMap;
import java.util.Map;


class CollectionProvider {
	private Map<String, RapidCollectionReference> mCollections = new HashMap<>();


	public <T> RapidCollectionReference<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<T>(new WebSocketCollectionConnection<>(rapid, collectionName, itemClass), collectionName, rapid.getHandler()));
		return mCollections.get(collectionName);
	}


	public RapidCollectionReference findCollectionByName(String collectionName) {
		return mCollections.get(collectionName);
	}


	public Map<String, RapidCollectionReference> getCollections() {
		return mCollections;
	}

}
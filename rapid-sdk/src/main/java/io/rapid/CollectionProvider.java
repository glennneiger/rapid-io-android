package io.rapid;


import java.util.HashMap;
import java.util.Map;


class CollectionProvider {
	private Map<String, RapidCollectionReference> mCollections = new HashMap<>();


	<T> RapidCollectionReference<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<T>(new WebSocketCollectionConnection<>(rapid, collectionName, itemClass), collectionName, rapid.getHandler()));
		return mCollections.get(collectionName);
	}


	RapidCollectionReference findCollectionByName(String collectionName) {
		return mCollections.get(collectionName);
	}


	Map<String, RapidCollectionReference> getCollections() {
		return mCollections;
	}


	void resubscribeAll() {
		for(RapidCollectionReference rapidCollectionReference : getCollections().values()) {
			if(rapidCollectionReference.isSubscribed()) {
				rapidCollectionReference.resubscribe();
			}
		}
	}
}
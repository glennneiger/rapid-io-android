package io.rapid;


import java.util.HashMap;
import java.util.Map;


class InMemoryCollectionProvider implements CollectionProvider {
	private Map<String, RapidCollectionReference> mCollections = new HashMap<>();


	@Override
	public <T> RapidCollectionReference<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollectionReference<T>(rapid, collectionName, itemClass));
		return mCollections.get(collectionName);
	}


	@Override
	public RapidCollectionReference findCollectionByName(String collectionName) {
		return mCollections.get(collectionName);
	}


	@Override
	public Map<String, RapidCollectionReference> getCollections() {
		return mCollections;
	}

}
package io.rapid;


import java.util.HashMap;
import java.util.Map;


class MockRapidCollectionProvider implements RapidCollectionProvider {
	private Map<String, RapidCollection> mCollections = new HashMap<>();


	@Override
	public <T> RapidCollection<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass) {
		if(!mCollections.containsKey(collectionName))
			mCollections.put(collectionName, new RapidCollection<T>(rapid, collectionName, itemClass));
		return mCollections.get(collectionName);
	}
}

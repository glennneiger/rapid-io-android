package io.rapid;


import java.util.Map;


interface CollectionProvider {
	<T> RapidCollectionReference<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass);
	RapidCollectionReference findCollectionByName(String collectionId);
	Map<String, RapidCollectionReference> getCollections();
}

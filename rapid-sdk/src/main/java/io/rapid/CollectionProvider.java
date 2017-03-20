package io.rapid;


interface CollectionProvider {
	<T> RapidCollectionReference<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass);
	RapidCollectionReference findCollectionByName(String collectionId);
}

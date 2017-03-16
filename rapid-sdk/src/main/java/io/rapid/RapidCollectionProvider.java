package io.rapid;


interface RapidCollectionProvider {
	<T> RapidCollection<T> provideCollection(Rapid rapid, String collectionName, Class<T> itemClass);
}

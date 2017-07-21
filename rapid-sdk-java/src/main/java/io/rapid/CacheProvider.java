package io.rapid;


interface CacheProvider {
	DiskCache getNewDiskCache(String apiKey);
	<T> MemoryCache<T> getNewMemoryCache(int maxValue);
}

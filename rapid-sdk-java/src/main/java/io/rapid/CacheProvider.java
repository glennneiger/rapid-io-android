package io.rapid;


public interface CacheProvider {
	DiskCache getNewDiskCache(String apiKey);
	<T> MemoryCache<T> getNewMemoryCache(int maxValue);
}

package io.rapid;


public interface MemoryCache<S> {
	void put(String key, S value);
	S get(String key);
	void evictAll();
	void remove(String key);
}

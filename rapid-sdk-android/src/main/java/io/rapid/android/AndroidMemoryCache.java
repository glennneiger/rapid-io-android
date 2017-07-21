package io.rapid.android;


import android.support.v4.util.LruCache;

import io.rapid.MemoryCache;


public class AndroidMemoryCache<T> implements MemoryCache<T> {
	LruCache<String, T> mCache;


	public AndroidMemoryCache(int maxSize) {
		mCache = new LruCache<>(maxSize);
	}


	@Override
	public void put(String key, T value) {
		mCache.put(key, value);
	}


	@Override
	public T get(String key) {
		return mCache.get(key);
	}


	@Override
	public void evictAll() {
		mCache.evictAll();
	}


	@Override
	public void remove(String key) {
		mCache.remove(key);
	}
}

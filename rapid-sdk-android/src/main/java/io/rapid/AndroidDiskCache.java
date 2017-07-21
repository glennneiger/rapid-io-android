package io.rapid;


import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;


public class AndroidDiskCache implements DiskCache {

	private static final int DEFAULT_INDEX = 0;
	private final DiskLruCache mCache;


	AndroidDiskCache(File file, int maxSizeInMb) throws IOException {
		mCache = DiskLruCache.open(file, 0, 1, maxSizeInMb * 1_000_000);
	}


	@Override
	public void delete() throws IOException {
		mCache.delete();
	}


	@Override
	public void setMaxSize(int maxSizeInBytes) {
		mCache.setMaxSize(maxSizeInBytes);
	}


	@Override
	public void remove(String key) throws IOException {
		mCache.remove(key);
	}


	@Override
	public String get(String key) throws IOException {
		DiskLruCache.Snapshot record = mCache.get(key);
		return record != null ? record.getString(DEFAULT_INDEX) : null;
	}


	@Override
	public void put(String key, String value) throws IOException {
		DiskLruCache.Editor editor = mCache.edit(key);
		editor.set(DEFAULT_INDEX, value);
		editor.commit();
	}
}

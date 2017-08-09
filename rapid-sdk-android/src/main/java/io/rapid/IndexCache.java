package io.rapid;


import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


class IndexCache {
	private static IndexCache sInstance;
	@NonNull private Map<String, List<String>> mCache = new HashMap<>();


	static IndexCache getInstance() {
		if(sInstance == null)
			sInstance = new IndexCache();
		return sInstance;
	}


	public void put(String className, List<String> indexList) {
		mCache.put(className, indexList);
	}


	public List<String> get(String className) {
		return mCache.get(className);
	}
}

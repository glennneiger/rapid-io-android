package io.rapid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public enum Sorting {
	ASC("asc"), DESC("desc");

	private String mKey;


	Sorting(String key) {
		mKey = key;
	}


	@NonNull
	static Sorting fromKey(@Nullable String key) {
		if(key == null) return ASC;

		for(Sorting item : Sorting.values()) {
			if(item.getKey().equalsIgnoreCase(key)) {
				return item;
			}
		}
		return ASC;
	}


	String getKey() {
		return mKey;
	}
}

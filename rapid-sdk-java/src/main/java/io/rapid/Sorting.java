package io.rapid;


public enum Sorting {
	ASC("asc"), DESC("desc");

	private String mKey;


	Sorting(String key) {
		mKey = key;
	}


	static Sorting fromKey(String key) {
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

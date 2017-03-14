package io.rapid;


public class Rapid {
	private static Rapid sInstance;
	private GsonConverter mJsonConverter;


	private Rapid() {
		mJsonConverter = new GsonConverter();
	}


	public static Rapid getInstance() {
		if(sInstance == null)
			sInstance = new Rapid();
		return sInstance;
	}


	public <T> RapidCollection<T> collection(String collectionName, Class<T> itemClass) {
		return new RapidCollection<>(this, collectionName);
	}


	public void setJsonConverter(GsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
	}
}

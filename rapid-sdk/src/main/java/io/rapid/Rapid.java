package io.rapid;


public class Rapid {
	private static Rapid sInstance;
	private final String mApiKey;
	private RapidJsonConverter mJsonConverter;


	private Rapid(String apiKey) {
		mApiKey = apiKey;
		mJsonConverter = new RapidGsonConverter();
	}


	public static Rapid getInstance() {
		if(sInstance == null)
			throw new IllegalStateException("Rapid SDK not initialized. Please call Rapid.initialize() first");
		return sInstance;
	}


	public static void initialize(String apiKey) {
		sInstance = new Rapid(apiKey);
	}


	public <T> RapidCollection<T> collection(String collectionName, Class<T> itemClass) {
		return new RapidCollection<>(this, collectionName);
	}


	public void setJsonConverter(RapidJsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
	}
}

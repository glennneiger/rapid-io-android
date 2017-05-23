package io.rapid;


import io.rapid.converter.RapidJsonConverter;


public class JsonConverterProvider {
	RapidJsonConverter mJsonConverter;


	public JsonConverterProvider(RapidJsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
	}


	public RapidJsonConverter get() {
		return mJsonConverter;
	}


	public void set(RapidJsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
	}
}

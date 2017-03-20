package io.rapid.converter;


import com.google.gson.Gson;


public class RapidGsonConverter implements RapidJsonConverter {

	Gson mGson;


	public RapidGsonConverter(Gson gson) {
		mGson = gson;
	}


	@Override
	public <T> T fromJson(String json, Class<T> type) {
		return mGson.fromJson(json, type);
	}


	@Override
	public <T> String toJson(T object) {
		return mGson.toJson(object);
	}
}

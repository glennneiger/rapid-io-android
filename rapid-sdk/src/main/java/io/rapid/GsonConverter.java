package io.rapid;


import com.google.gson.Gson;


public class GsonConverter implements RapidJsonConverter {

	Gson mGson = new Gson();


	@Override
	public <T> T fromJson(String json, Class<T> type) {
		return mGson.fromJson(json, type);
	}
}

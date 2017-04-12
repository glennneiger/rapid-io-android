package io.rapid;


import org.json.JSONObject;

import java.io.IOException;

import io.rapid.converter.RapidJsonConverter;


public class RapidDocument<T> {
	public static final String KEY_ID = "id";
	public static final String KEY_BODY = "body";
	private String id;
	private T body;


	RapidDocument(String id, T value) {
		this.id = id;
		body = value;
	}


	static <T> RapidDocument<T> fromJsonObject(JSONObject jsonObject, RapidJsonConverter jsonConverter, Class<T> documentType) throws IOException {
		return new RapidDocument<T>(jsonObject.optString(KEY_ID), jsonConverter.fromJson(jsonObject.optString(KEY_BODY), documentType));
	}


	@Override
	public String toString() {
		return "RapidDocument(" + getId() + ": " + getBody().toString() + ")";
	}


	public String getId() {
		return id;
	}


	public T getBody() {
		return body;
	}
}

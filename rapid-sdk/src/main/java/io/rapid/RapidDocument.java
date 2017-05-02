package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.rapid.converter.RapidJsonConverter;


public class RapidDocument<T> {
	public static final String KEY_ID = "id";
	public static final String KEY_SKEY = "skey";
	public static final String KEY_CRT = "crt";
	public static final String KEY_BODY = "body";
	private String id;
	private int createdTimestamp;
	private List<String> sorting;
	private T body;


	RapidDocument(String id, List<String> sortingKey, int createdTimestamp, T value) {
		this.id = id;
		this.sorting = sortingKey;
		this.createdTimestamp = createdTimestamp;
		body = value;
	}


	RapidDocument(String id, T value) {
		this.id = id;
		body = value;
	}


	static <T> RapidDocument<T> fromJsonObject(JSONObject jsonObject, RapidJsonConverter jsonConverter, Class<T> documentType)
			throws	IOException {
		JSONArray sortingJSONArray = jsonObject.optJSONArray(KEY_SKEY);
		List<String> sortingList = new ArrayList<>();
		if(sortingJSONArray != null)
		{
			for(int i = 0; i < sortingJSONArray.length(); i++) {
				sortingList.add(sortingJSONArray.optString(i));
			}
		}

		int createdTimestamp = jsonObject.optInt(KEY_CRT);
		sortingList.add(Integer.toString(createdTimestamp));

		return new RapidDocument<T>(jsonObject.optString(KEY_ID), sortingList, createdTimestamp,
				jsonConverter.fromJson(jsonObject.optString(KEY_BODY), documentType));
	}


	public String toJson(RapidJsonConverter jsonConverter)
	{
		try
		{
			JSONObject jsonBody = new JSONObject();
			jsonBody.put(KEY_ID, id);
			jsonBody.put(KEY_BODY, new JSONObject(jsonConverter.toJson(body)));

			return jsonBody.toString();
		}
		catch(IOException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch(JSONException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	@Override
	public String toString() {
		return "RapidDocument(" + getId() + ": " + getBody().toString() + ")";
	}


	public String getId() {
		return id;
	}


	public int getCreatedTimestamp()
	{
		return createdTimestamp;
	}


	public T getBody() {
		return body;
	}


	public List<String> getSorting()
	{
		return sorting;
	}
}

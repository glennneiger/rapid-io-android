package io.rapid;


import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.rapid.converter.RapidJsonConverter;


public class RapidDocument<T> implements Comparable<RapidDocument<T>> {
	static final String KEY_ID = "id";
	static final String KEY_BODY = "body";
	private static final String KEY_SKEY = "skey";
	private static final String KEY_CRT = "crt";
	private String id;
	private long createdTimestamp;
	private List<String> sorting;
	private EntityOrder order;
	private T body;


	RapidDocument(String id, List<String> sortingKey, long createdTimestamp, T value) {
		this.id = id;
		this.sorting = sortingKey;
		this.createdTimestamp = createdTimestamp;
		this.body = value;
		this.sorting.add(Long.toString(this.createdTimestamp));
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
		return new RapidDocument<>(jsonObject.optString(KEY_ID), sortingList, jsonObject.optLong(KEY_CRT),
				jsonConverter.fromJson(jsonObject.optString(KEY_BODY), documentType));
	}


	@Override
	public int compareTo(@NonNull RapidDocument<T> doc) {
		int depth = 0;
		while(sorting.get(depth).compareTo(doc.getSorting().get(depth)) == 0)
		{
			depth++;

			if(depth == sorting.size())
			{
				depth--;
				break;
			}
		}

		Sorting sortingType;
		if(depth == sorting.size() - 1)
			sortingType = Sorting.ASC;
		else
			sortingType = order.getOrderList().get(depth).getSorting();

		if(sortingType == Sorting.ASC)
			return sorting.get(depth).compareTo(doc.getSorting().get(depth));
		else
			return -sorting.get(depth).compareTo(doc.getSorting().get(depth));
	}


	String toJson(RapidJsonConverter jsonConverter)
	{
		try
		{
			JSONObject jsonBody = new JSONObject();
			jsonBody.put(KEY_ID, id);
			jsonBody.put(KEY_BODY, new JSONObject(jsonConverter.toJson(body)));

			return jsonBody.toString();
		}
		catch(IOException | JSONException e)
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


	public long getCreatedTimestamp()
	{
		return createdTimestamp;
	}


	public T getBody() {
		return body;
	}


	List<String> getSorting()
	{
		return sorting;
	}


	EntityOrder getOrder()
	{
		return order;
	}


	void setOrder(EntityOrder order)
	{
		this.order = order;
	}
}

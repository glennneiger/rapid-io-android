package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RapidDocument<T> implements Comparable<RapidDocument<T>> {
	static final String KEY_ID = "id";
	static final String KEY_BODY = "body";
	private static final String KEY_SKEY = "skey";
	private static final String KEY_CRT = "crt";
	private static final String KEY_ETAG = "etag";
	private Collection<String> mFillWithTimestampProperties;
	private String id;
	private String createdTimestamp;
	private List<String> sorting;
	private EntityOrder order;
	private Etag etag;
	private T body;


	RapidDocument(String id, List<String> sortingKey, String createdTimestamp, Etag etag, T value) {
		this.id = id;
		this.sorting = sortingKey;
		this.createdTimestamp = createdTimestamp;
		this.etag = etag;
		this.body = value;
		this.sorting.add(this.createdTimestamp);
	}


	RapidDocument(String id, T value, RapidMutateOptions options) {
		this.id = id;
		body = value;
		if (options != null) {
			this.etag = options.getExpectedEtag();
			mFillWithTimestampProperties = options.getFillWithTimestampProperties();
		}
	}


	static <T> RapidDocument<T> fromJsonObject(JSONObject jsonObject, JsonConverterProvider jsonConverter, Class<T> documentType)
			throws	IOException {
		JSONArray sortingJSONArray = jsonObject.optJSONArray(KEY_SKEY);
		List<String> sortingList = new ArrayList<>();
		if(sortingJSONArray != null)
		{
			for(int i = 0; i < sortingJSONArray.length(); i++) {
				sortingList.add(sortingJSONArray.optString(i));
			}
		}
		return new RapidDocument<>(jsonObject.optString(KEY_ID), sortingList, jsonObject.optString(KEY_CRT), Etag.fromValue(jsonObject.optString(KEY_ETAG)),
				jsonConverter.get().fromJson(jsonObject.optString(KEY_BODY), documentType));
	}


	@Override
	public int compareTo(RapidDocument<T> doc) {
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
		if (order == null){
			sortingType = Sorting.ASC;
		} else if(depth == sorting.size() - 1) // last sorting by created_timestamp
			// TODO: temp fix until DocumentDB supports multiple order
			// Document DB can sort by one attribute for now. Secondary sort is by 'crt' attribute but the direction is the same
			// Therefore we need to get sorting type form the first sorting in order list temporarily
			sortingType = order.getOrderList().isEmpty() ? Sorting.ASC : order.getOrderList().get(0).getSorting();
//			sortingType = Sorting.ASC;
		else
			sortingType = order.getOrderList().get(depth).getSorting();

		if(sortingType == Sorting.ASC)
			return sorting.get(depth).compareTo(doc.getSorting().get(depth));
		else
			return -sorting.get(depth).compareTo(doc.getSorting().get(depth));
	}


	String toJson(JsonConverterProvider jsonConverter)
	{
		try
		{
			JSONObject jsonBody = new JSONObject();
			jsonBody.put(KEY_ID, id);
			if(etag != null)
				jsonBody.put(KEY_ETAG, etag.getSerialized());
			if (body != null)
				jsonBody.put(KEY_BODY, new JSONObject(jsonConverter.get().toJson(body)));


			// handle server values
			// timestamp
			if(mFillWithTimestampProperties != null) {
				if(jsonBody.optJSONObject(KEY_BODY) != null) {
					for(String fillWithTimestampProperty : mFillWithTimestampProperties) {
						JSONObject replacePosition = jsonBody.getJSONObject(KEY_BODY);
						String[] parts = fillWithTimestampProperty.contains(".") ? fillWithTimestampProperty.split("\\.") : new String[]{fillWithTimestampProperty};
						for(int i = 0; i < parts.length; i++) {
							String part = parts[i];
							if (part.isEmpty()) continue;
							if(i != parts.length - 1) {
								if(replacePosition.optJSONObject(part) == null)
									replacePosition.put(part, new JSONObject());
								replacePosition = replacePosition.optJSONObject(part);
							} else {
								replacePosition.put(part, ServerValue.TIMESTAMP);
							}
						}
					}
				}
			}

			return jsonBody.toString();
		}
		catch(IOException | JSONException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public boolean hasSameContentAs(RapidDocument otherDocument) {
		if(!this.id.equals(otherDocument.id))
			return false;
		else if(this.etag == null || otherDocument.etag == null)
			return false;
		else
			return this.etag.equals(otherDocument.etag);
	}


	@Override
	public String toString() {
		return "RapidDocument(" + getId() + ": " + getBody().toString() + ")";
	}


	public String getId() {
		return id;
	}


	public Etag getEtag() {
		return etag;
	}


	String getCreatedTimestamp()
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

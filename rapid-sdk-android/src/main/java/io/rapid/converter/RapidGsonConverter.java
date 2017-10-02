package io.rapid.converter;


import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class RapidGsonConverter implements RapidJsonConverter {
	private Gson mGson;


	public RapidGsonConverter() {
		this(new GsonBuilder());
	}


	@SuppressWarnings("WeakerAccess")
	public RapidGsonConverter(@NonNull GsonBuilder gsonBuilder) {
		mGson = gsonBuilder
				.serializeNulls()
				.registerTypeAdapter(Date.class, new GsonUtcDateAdapter())
				.create();
	}


	@Override
	public <T> T fromJson(String json, @NonNull Class<T> type) {
		return mGson.fromJson(json, type);
	}


	@Override
	public <T> String toJson(T object) {
		return mGson.toJson(object);
	}


	static class GsonUtcDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

		@NonNull private final DateFormat dateFormat;


		GsonUtcDateAdapter() {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);      //This is the format I need
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));                               //This is the key line which converts the date to UTC which cannot be accessed with the default serializer
		}


		@NonNull
		@Override
		public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(dateFormat.format(date));
		}


		@Override
		public synchronized Date deserialize(@NonNull JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			try {
				return dateFormat.parse(jsonElement.getAsString());
			} catch(ParseException e) {
				throw new JsonParseException(e);
			}
		}
	}
}

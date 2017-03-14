package io.rapid;


public interface RapidJsonConverter {
	<T> T fromJson(String json, Class<T> type);
}

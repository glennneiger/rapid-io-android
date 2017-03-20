package io.rapid.converter;


import java.io.IOException;


public interface RapidJsonConverter {
	<T> T fromJson(String json, Class<T> type) throws IOException;
	<T> String toJson(T object) throws IOException;
}

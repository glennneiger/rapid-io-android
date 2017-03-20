package io.rapid.converter;


import java.io.IOException;
import java.lang.reflect.Type;


public interface RapidJsonConverter {
	<T> T fromJson(String json, Class<T> type) throws IOException;
	<T> T fromJson(String json, Type type) throws IOException;
	<T> String toJson(T object) throws IOException;
}

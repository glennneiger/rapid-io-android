package io.rapid.converter;


import java.io.IOException;


/**
 * TODO
 * <p>
 * (note: every implementation of this interface should serialize {@link java.util.Date} into lexicographically sortable String in order
 * to be able to make queries based on Date properties)
 */
public interface RapidJsonConverter {
	<T> T fromJson(String json, Class<T> type) throws IOException;
	<T> String toJson(T object) throws IOException;
}

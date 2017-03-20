package io.rapid.sample;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import io.rapid.RapidWrapper;
import io.rapid.converter.RapidJsonConverter;


public class RapidJacksonConverter implements RapidJsonConverter {

	ObjectMapper mMapper = new ObjectMapper();


	@Override
	public <T> T fromJson(String json, Class<T> type) throws IOException {
		return mMapper.readValue(json, type);
	}


	@Override
	public <T> String toJson(T object) throws JsonProcessingException {
		return mMapper.writeValueAsString(object);
	}
}

package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;


class FilterValue implements Filter {
	private String property;
	private PropertyValue value;


	interface PropertyValue {
		String TYPE_EQUAL = "eq";
		String TYPE_GREATER_THAN = "gt";
		String TYPE_GREATER_OR_EQUAL_THAN = "gte";
		String TYPE_LESS_THAN = "lt";
		String TYPE_LESS_OR_EQUAL_THAN = "lte";
		String TYPE_CONTAINS = "cnt";
		String TYPE_STARTS_WITH = "pref";
		String TYPE_ENDS_WITH = "suf";
		String TYPE_ARRAY_CONTAINS = "arr-cnt";

		Object toJson() throws JSONException;
	}


	FilterValue(String property, PropertyValue value) {
		this.property = property;
		this.value = value;
	}


	@Override
	public String toJson() throws JSONException {
		JSONObject root = new JSONObject();
		Object json = value.toJson();
		try {
			root.put(property, new JSONObject((String) json));
		} catch(Exception e) {
			root.put(property, json);
		}
		return root.toString();
	}


	static class IntPropertyValue implements PropertyValue {

		private final String compareType;
		private int value;


		IntPropertyValue(String compareType, int value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public Object toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return value;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}

	static class LongPropertyValue implements PropertyValue {

		private final String compareType;
		private long value;


		LongPropertyValue(String compareType, long value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public Object toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return value;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class StringPropertyValue implements PropertyValue {

		private final String compareType;
		private String value;


		public StringPropertyValue(String compareType, String value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public Object toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return value != null ? value : JSONObject.NULL;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value != null ? value : JSONObject.NULL);
				return root.toString();
			}
		}
	}

	static class NullPropertyValue implements PropertyValue {

		private final String compareType;


		public NullPropertyValue(String compareType) {
			this.compareType = compareType;
		}


		@Override
		public Object toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return JSONObject.NULL;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, JSONObject.NULL);
				return root.toString();
			}
		}
	}


	static class DoublePropertyValue implements PropertyValue {

		private final String compareType;
		private double value;


		DoublePropertyValue(String compareType, double value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public Object toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return value;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class BooleanPropertyValue implements PropertyValue {

		private boolean value;


		BooleanPropertyValue(boolean value) {
			this.value = value;
		}


		@Override
		public Boolean toJson() throws JSONException {
			return value;
		}
	}


	static class DatePropertyValue implements PropertyValue {

		private final JsonConverterProvider jsonConverter;
		private final String compareType;
		private Date value;


		DatePropertyValue(String compareType, Date value, JsonConverterProvider jsonConverter) {
			this.value = value;
			this.jsonConverter = jsonConverter;
			this.compareType = compareType;
		}


		@Override
		public Object toJson() throws JSONException {
			try {
				if(compareType.equals(TYPE_EQUAL)) {
					return value != null ? jsonConverter.get().toJson(value) : JSONObject.NULL;
				} else {
					JSONObject root = new JSONObject();
					root.put(compareType, value != null ? jsonConverter.get().toJson(value) : JSONObject.NULL);
					return root.toString();
				}
			} catch(IOException e) {
				return value.toString();
			}
		}
	}


	static class ListPropertyValue<T> implements PropertyValue {

		private final String compareType;
		private List<T> value;


		ListPropertyValue(String compareType, List<T> value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public String toJson() throws JSONException {
			JSONObject root = new JSONObject();
			JSONArray list = new JSONArray();
			for(T item : value) {
				list.put(item);
			}
			root.put(compareType, list);
			return root.toString();
		}
	}
}

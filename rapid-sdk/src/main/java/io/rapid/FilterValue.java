package io.rapid;


import org.json.JSONException;
import org.json.JSONObject;


class FilterValue implements Filter {
	private String property;
	private PropertyValue value;


	interface PropertyValue {
		String TYPE_EQUAL = "eq";
		String TYPE_NOT_EQUAL = "neq";
		String TYPE_GREATER_THAN = "gt";
		String TYPE_GREATER_OR_EQUAL_THAN = "gte";
		String TYPE_LESS_THAN = "lt";
		String TYPE_LESS_OR_EQUAL_THAN = "lte";

		Object toJson() throws JSONException;
	}


	public FilterValue(String property, PropertyValue value) {
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


		public IntPropertyValue(String compareType, int value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public String toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return String.valueOf(value);
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
		public String toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return value;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class DoublePropertyValue implements PropertyValue {

		private final String compareType;
		private double value;


		public DoublePropertyValue(String compareType, double value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public String toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUAL)) {
				return String.valueOf(value);
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class BooleanPropertyValue implements PropertyValue {

		private boolean value;


		public BooleanPropertyValue(boolean value) {
			this.value = value;
		}


		@Override
		public Boolean toJson() throws JSONException {
			return value;
		}
	}
}

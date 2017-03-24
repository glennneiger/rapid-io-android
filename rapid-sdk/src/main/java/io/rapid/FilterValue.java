package io.rapid;


import org.json.JSONException;
import org.json.JSONObject;


class FilterValue implements Filter {
	private String property;
	private PropertyValue value;


	interface PropertyValue {
		String TYPE_EQUALS = "eq";
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


	static class IntComparePropertyValue implements PropertyValue {

		private final String compareType;
		private int value;


		public IntComparePropertyValue(String compareType, int value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public String toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUALS)) {
				return String.valueOf(value);
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class StringComparePropertyValue implements PropertyValue {

		private final String compareType;
		private String value;


		public StringComparePropertyValue(String compareType, String value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public String toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUALS)) {
				return value;
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class DoubleComparePropertyValue implements PropertyValue {

		private final String compareType;
		private double value;


		public DoubleComparePropertyValue(String compareType, double value) {
			this.value = value;
			this.compareType = compareType;
		}


		@Override
		public String toJson() throws JSONException {
			if(compareType.equals(TYPE_EQUALS)) {
				return String.valueOf(value);
			} else {
				JSONObject root = new JSONObject();
				root.put(compareType, value);
				return root.toString();
			}
		}
	}


	static class BooleanComparePropertyValue implements PropertyValue {

		private boolean value;


		public BooleanComparePropertyValue(boolean value) {
			this.value = value;
		}


		@Override
		public Boolean toJson() throws JSONException {
			return value;
		}
	}
}

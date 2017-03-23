package io.rapid;


class FilterValue implements Filter {
	private String property;
	private PropertyValue value;


	public FilterValue(String property, PropertyValue value) {
		this.property = property;
		this.value = value;
	}


	static class PropertyValue{

	}

	static class StringPropertyValue extends PropertyValue{
		String value;


		public StringPropertyValue(String value) {
			this.value = value;
		}
	}
	class IntPropertyValue extends PropertyValue{
		int value;
	}
	static class GreaterThanPropertyValue extends PropertyValue{
		int value;


		public GreaterThanPropertyValue(int value) {
			this.value = value;
		}
	}
}

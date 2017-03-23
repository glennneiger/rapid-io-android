package io.rapid;

import org.junit.Test;


public class FilterJsonTest {
	@Test
	public void test_1() throws Exception {
		String json = new FilterAnd(
				new FilterOr(
						new FilterValue("age", new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, 18)),
						new FilterValue("age", new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, 60))
				),
				new FilterValue("name", new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUALS, "Debil"))
		).toJson();

		System.out.println(json);
	}


	@Test
	public void test_2() throws Exception {
		String json = new FilterValue("name", new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUALS, "Debil")).toJson();

		System.out.println(json);
	}
}
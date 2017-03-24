package io.rapid;

import org.junit.Test;


public class FilterJsonTest extends BaseTest {
	@Test
	public void test_1() throws Exception {
		String json = new FilterAnd(
				new FilterOr(
						new FilterValue("age", new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, 18)),
						new FilterValue("age", new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, 60))
				),
				new FilterValue("name", new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, "Debil"))
		).toJson();

		System.out.println(json);
	}


	@Test
	public void test_2() throws Exception {
		String json = new FilterValue("name", new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, "Debil")).toJson();

		System.out.println(json);
	}

	@Test
	public void test_3() throws Exception {
		RapidCollectionReference<Object> collection = new RapidCollectionReference<>(new MockCollectionConnection<>(), "collection");
		collection
				.equalTo("type", "SUV")
				.beginOr()
					.greaterOrEqualThan("size", 3)
					.lessThan("size", 2)
					.beginAnd()
						.beginOr()
							.equalTo("open", "24/7")
							.lessOrEqualThan("time", 3)
						.endOr()
						.equalTo("transmission", "automatic")
					.endAnd()
					.equalTo("enabled", true)
				.endOr()
				.skip(10)
				.limit(50)
				.orderBy("type")
				.orderBy("price", Sorting.DESC)
				.lessOrEqualThan("test", 123);

		print("Filter:");
		printJson(collection.getFilter().toJson());
		print("Limit:");
		print(collection.getLimit());
		print("Skip:");
		print(collection.getSkip());
		print("Order:");
		printJson(collection.getOrder().toJson());
	}


}
package io.rapid;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.rapid.base.BaseTest;

import static junit.framework.Assert.assertEquals;


public class FilterJsonTest extends BaseTest {
	@Test
	public void test_model2json_1() throws Exception {
		String json = new FilterAnd(
				new FilterOr(
						new FilterValue("age", new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, 18)),
						new FilterValue("age", new FilterValue.IntComparePropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, 60))
				),
				new FilterValue("name", new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, "John"))
		).toJson();

		JSONAssert.assertEquals(json, "{\"and\":[{\"or\":[{\"age\":{\"lt\":18}},{\"age\":{\"gt\":60}}]},{\"name\":\"John\"}]}", false);
	}


	@Test
	public void test_query2json_1() throws Exception {
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


		JSONAssert.assertEquals(
				collection.getFilter().toJson(),
				"{\"and\":[{\"type\":\"SUV\"},{\"or\":[{\"size\":{\"gte\":3}},{\"size\":{\"lt\":2}},{\"and\":[{\"or\":[{\"open\":\"24/7\"},{\"time\":{\"lte\":3}}]},{\"transmission\":\"automatic\"}]},{\"enabled\":true}]},{\"test\":{\"lte\":123}}]}",
				false
		);
		assertEquals(collection.getLimit(), 50);
		assertEquals(collection.getSkip(), 10);
		assertEquals(collection.getOrder().toJson().toString(), "[{\"type\":\"asc\"},{\"price\":\"desc\"}]");
	}


}
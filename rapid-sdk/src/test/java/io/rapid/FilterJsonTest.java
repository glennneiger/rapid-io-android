package io.rapid;

import android.os.Handler;
import android.support.annotation.NonNull;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.List;

import io.rapid.base.BaseTest;
import io.rapid.converter.RapidGsonConverter;

import static junit.framework.Assert.assertEquals;


public class FilterJsonTest extends BaseTest {
	@Test
	public void test_model2json_1() throws Exception {
		String json = new Filter.And(
				new Filter.Or(
						new FilterValue("age", new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_LESS_THAN, 18)),
						new FilterValue("age", new FilterValue.IntPropertyValue(FilterValue.PropertyValue.TYPE_GREATER_THAN, 60))
				),
				new FilterValue("name", new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, "John"))
		).toJson();

		JSONAssert.assertEquals(json, "{\"and\":[{\"or\":[{\"age\":{\"lt\":18}},{\"age\":{\"gt\":60}}]},{\"name\":\"John\"}]}", false);
	}


	@Test
	public void test_query2json_1() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
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
				.lessOrEqualThan("test", 123)
				.subscribe(rapidDocuments -> {
				});


		String json = "{\"and\":[{\"type\":\"SUV\"},{\"or\":[{\"size\":{\"gte\":3}},{\"size\":{\"lt\":2}},{\"and\":[{\"or\":[{\"open\":\"24/7\"},{\"time\":{\"lte\":3}}]},{\"transmission\":\"automatic\"}]},{\"enabled\":true}]},{\"test\":{\"lte\":123}}]}";
		JSONAssert.assertEquals(
				subscription.getFilter().toJson(),
				json,
				false
		);
		assertEquals(subscription.getLimit(), 50);
		assertEquals(subscription.getSkip(), 10);
		JSONAssert.assertEquals(subscription.getOrder().toJson().toString(), "[{\"type\":\"asc\"},{\"price\":\"desc\"}]", false);
	}


	@Test
	public void test_query2json_2() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
				.lessOrEqualThan("mileage", 34.4)
				.beginAnd()
				.between("price", 321212.3, 1213123.2)
				.equalTo("size", 121)
				.endAnd()
				.subscribe(rapidDocuments -> {
				});

		String json = "{\"and\":[{\"mileage\":{\"lte\":34.4}},{\"and\":[{\"and\":[{\"price\":{\"gte\":321212.3}},{\"price\":{\"lte\":1213123.2}}]},{\"size\":\"121\"}]}]}";
		JSONAssert.assertEquals(subscription.getFilter().toJson(), json, false);
	}


	@Test(expected = IllegalArgumentException.class)
	public void test_query2json_3() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
				.beginAnd()
				.beginAnd()
				.beginAnd()
				.beginAnd()
				.endAnd()
				.subscribe(rapidDocuments -> {
				});
		subscription.getFilter();
	}


	@Test(expected = IllegalArgumentException.class)
	public void test_query2json_4() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
				.beginAnd()
				.endOr()
				.subscribe(rapidDocuments -> {
				});
		subscription.getFilter();
	}


	@Test(expected = IllegalArgumentException.class)
	public void test_query2json_5() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
				.beginNot()
				.endAnd()
				.subscribe(rapidDocuments -> {
				});
		subscription.getFilter();
	}


	@Test
	public void test_query2json_6() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection().idEqualTo("123").idNotEqualTo("223").subscribe(rapidDocuments -> {
		});

		String json = "{\"and\":[{\"$id\":\"123\"},{\"$id\":{\"neq\":\"223\"}}]}";
		JSONAssert.assertEquals(subscription.getFilter().toJson(), json, false);
	}


	@Test
	public void test_query2json_7() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
				.beginAnd()
				.between("price", 3, 5.9)
				.endAnd()
				.subscribe(rapidDocuments -> {
				});

		String json = "{\"and\":[{\"and\":[{\"and\":[{\"price\":{\"gte\":3}},{\"price\":{\"lte\":5.9}}]}]}]}";
		JSONAssert.assertEquals(subscription.getFilter().toJson(), json, false);
	}


	@Test
	public void test_query2json_8() throws Exception {
		RapidCollectionSubscription subscription = getNewCollection()
				.beginOr()
				.equalTo("model", "A5")
				.equalTo("model", "A7")
				.endOr()
				.beginAnd()
				.lessOrEqualThan("price", 10000)
				.lessOrEqualThan("hp", 400)
				.endAnd()
				.subscribe(rapidDocuments -> {
				});

		String json = "{\"and\":[{\"or\":[{\"model\":\"A5\"},{\"model\":\"A7\"}]},{\"and\":[{\"price\":{\"lte\":10000}},{\"hp\":{\"lte\":400}}]}]}";
		JSONAssert.assertEquals(subscription.getFilter().toJson(), json, false);
	}


	@Test
	public void test_multiple_subscribes_1() throws Exception {

		RapidCollectionReference<Object> col = getNewCollection();

		RapidCollectionSubscription subscription = col
				.equalTo("model", "A5")
				.orderBy("p1")
				.skip(10)
				.subscribe(rapidDocuments -> {
				});
		JSONAssert.assertEquals(subscription.getFilter().toJson(), "{\"and\":[{\"model\":\"A5\"}]}", false);
		JSONAssert.assertEquals(subscription.getOrder().toJson().toString(), "[{\"p1\":\"asc\"}]", false);
		assertEquals(10, subscription.getSkip());

		RapidCollectionSubscription subscription2 = col
				.equalTo("model2", "A1")
				.orderBy("p2")
				.subscribe(rapidDocuments -> {
				});
		JSONAssert.assertEquals(subscription2.getFilter().toJson(), "{\"and\":[{\"model2\":\"A1\"}]}", false);
		JSONAssert.assertEquals(subscription2.getOrder().toJson().toString(), "[{\"p2\":\"asc\"}]", false);
		assertEquals(0, subscription2.getSkip());

	}


	@Test
	public void test_array_filters() throws Exception {
		List<String> values = Arrays.asList("a", "b", "c");
		Subscription sub = getNewCollection()
				.in("prop", values).subscribe(rapidDocuments -> {});

		String json = "{\"and\":[{\"prop\":{\"arr-cnt\":[\"a\",\"b\",\"c\"]}}]}";

		assertEquals(sub.getFilter().toJson(), json);
	}


	@NonNull
	private RapidCollectionReference<Object> getNewCollection() {return new RapidCollectionReference<>(new MockCollectionConnection<>(), "collection", new Handler(), new RapidGsonConverter());}


}
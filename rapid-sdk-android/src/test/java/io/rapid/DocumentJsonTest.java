package io.rapid;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import io.rapid.base.BaseTest;
import io.rapid.converter.RapidGsonConverter;

import static org.junit.Assert.assertEquals;


public class DocumentJsonTest extends BaseTest {


	private JsonConverterProvider mJsonConverter;


	@Before
	public void setUp() throws Exception {
		mJsonConverter = new JsonConverterProvider(new RapidGsonConverter());
	}


	@Test
	public void test_doc_2_json() throws Exception {
		TestObject testObject = new TestObject(0, "test", new SubTestObject(0, "subtest"));

		RapidMutateOptions options = new RapidMutateOptions.Builder()
				.fillPropertyWithServerTimestamp("timestamp")
				.fillPropertyWithServerTimestamp("sub.timestamp")
				.fillPropertyWithServerTimestamp("notexisting.notexisting.timestamp")
				.build();
		RapidDocument<TestObject> doc = new RapidDocument<>("id", testObject, options);

		// assert
		JSONObject json = new JSONObject(doc.toJson(mJsonConverter));
		assertEquals(ServerValue.TIMESTAMP, json.getJSONObject("body").getString("timestamp"));
		assertEquals(ServerValue.TIMESTAMP, json.getJSONObject("body").getJSONObject("sub").getString("timestamp"));
		assertEquals(ServerValue.TIMESTAMP, json.getJSONObject("body").getJSONObject("notexisting").getJSONObject("notexisting").getString("timestamp"));
	}


	class SubTestObject {
		long timestamp;
		String name;


		public SubTestObject(long timestamp, String name) {
			this.timestamp = timestamp;
			this.name = name;
		}
	}


	class TestObject {
		long timestamp;
		String name;
		SubTestObject sub;


		public TestObject(long timestamp, String name, SubTestObject sub) {
			this.timestamp = timestamp;
			this.name = name;
			this.sub = sub;
		}
	}


}
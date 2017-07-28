package io.rapid.base;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BaseTest {
	protected void print(int message) {
		System.out.println(message);
	}


	protected void print(String message, Object... attrs) {
		System.out.println(String.format(message, attrs));
	}


	protected void printJson(String json) throws JSONException {
		JSONObject object = new JSONObject(json);
		System.out.println(object.toString());
	}


	protected void printJson(JSONArray jsonArray) throws JSONException {
		System.out.println(jsonArray.toString());
	}


	private void printJson(JSONObject jsonObject) throws JSONException {
		System.out.println(jsonObject.toString());
	}
}

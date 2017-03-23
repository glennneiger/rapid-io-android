package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class BaseTest {
	protected void print(int message) {
		System.out.println(message);
	}


	protected void print(String message) {
		System.out.println(message);
	}


	protected void printJson(String json) throws JSONException {
		JSONObject object = new JSONObject(json);
		System.out.println(object.toString(4));
	}


	protected void printJson(JSONArray jsonArray) throws JSONException {
		System.out.println(jsonArray.toString(4));
	}


	private void printJson(JSONObject jsonObject) throws JSONException {
		System.out.println(jsonObject.toString(4));
	}
}

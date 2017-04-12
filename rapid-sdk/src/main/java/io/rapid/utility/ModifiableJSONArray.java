package io.rapid.utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ModifiableJSONArray extends JSONArray {

	public ModifiableJSONArray(String json) throws JSONException {
		super(json);
	}


	public ModifiableJSONArray() {
		super();
	}


	public static ModifiableJSONArray removeItem(JSONArray jsonArray, int index) throws JSONException {

		ModifiableJSONArray output = new ModifiableJSONArray();
		int len = jsonArray.length();
		for(int i = 0; i < len; i++) {
			if(i != index) {
				try {
					output.put(jsonArray.get(i));
				} catch(JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return output;
	}


	public void add(int pos, JSONObject jsonObj) throws JSONException {
		for(int i = length(); i > pos; i--) {
			put(i, get(i - 1));
		}
		put(pos, jsonObj);
	}
}
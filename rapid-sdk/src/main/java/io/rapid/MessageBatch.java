package io.rapid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Leos on 22.03.2017.
 */

class MessageBatch extends MessageBase {
	private List<MessageBase> mMessageList = new ArrayList<>();


	MessageBatch() {
		super(MessageType.BATCH);
	}


	MessageBatch(JSONObject json) throws JSONException {
		super(MessageType.BATCH);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for(MessageBase message : mMessageList) {
			array.put(message.toJson());
		}

		json.put(getMessageType().getKey(), array);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException {
		JSONArray array = json.optJSONArray(getMessageType().getKey());
		for(int i = 0; i < array.length(); i++) {
			mMessageList.add(MessageParser.parse(array.optString(i)));
		}

	}


	public List<MessageBase> getMessageList() {
		return mMessageList;
	}


	public void addMessage(MessageBase message) {
		mMessageList.add(message);
	}
}

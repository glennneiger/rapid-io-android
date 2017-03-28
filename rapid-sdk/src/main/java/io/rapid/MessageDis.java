package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageDis extends MessageBase {


	public MessageDis(String eventId) {
		super(MessageType.DIS, eventId);
	}


	public MessageDis(JSONObject json) throws JSONException {
		super(MessageType.DIS);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);
	}
}

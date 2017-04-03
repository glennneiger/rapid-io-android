package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageNop extends MessageBase {
	public MessageNop(String eventId) {
		super(MessageType.NOP, eventId);
	}


	public MessageNop(JSONObject json) throws JSONException {
		super(MessageType.NOP);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(getMessageType().getKey(), JSONObject.NULL);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);
	}
}

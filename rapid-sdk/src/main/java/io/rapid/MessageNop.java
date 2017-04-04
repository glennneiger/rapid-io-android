package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;

class MessageNop extends MessageBase {
	public MessageNop() {
		super(MessageType.NOP, "");
	}


	public MessageNop(JSONObject json) throws JSONException {
		super(MessageType.NOP, json);
	}
}

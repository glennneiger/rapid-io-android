package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageAck extends MessageBase {

	MessageAck(String eventId) {
		super(MessageType.ACK, eventId);
	}


	MessageAck(JSONObject json) throws JSONException {
		super(MessageType.ACK, json);
	}
}

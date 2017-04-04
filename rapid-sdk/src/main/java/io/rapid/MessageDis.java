package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageDis extends MessageBase {


	public MessageDis() {
		super(MessageType.DIS);
	}


	public MessageDis(JSONObject json) throws JSONException {
		super(MessageType.DIS, json);
	}
}

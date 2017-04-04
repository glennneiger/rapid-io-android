package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageCon extends MessageBase {
	private static final String ATTR_CON_ID = "con-id";

	private String mConnectionId;


	public MessageCon(String connectionId, boolean reconnect) {
		super(reconnect ? MessageType.REC : MessageType.CON);
		mConnectionId = connectionId;
	}


	public MessageCon(JSONObject json) throws JSONException {
		super(MessageType.CON, json);
	}


	@Override
	protected JSONObject createJsonBody() throws JSONException {
		JSONObject body = super.createJsonBody();
		body.put(ATTR_CON_ID, getConnectionId());
		return body;
	}


	@Override
	protected void parseJsonBody(JSONObject jsonBody) {
		super.parseJsonBody(jsonBody);
		mConnectionId = jsonBody.optString(ATTR_CON_ID);
	}


	public String getConnectionId() {
		return mConnectionId;
	}
}

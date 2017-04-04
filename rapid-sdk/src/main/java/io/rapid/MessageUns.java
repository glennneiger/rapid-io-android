package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageUns extends MessageBase {
	private static final String ATTR_SUB_ID = "sub-id";

	private String mSubscriptionId;


	public MessageUns(String subscriptionId) {
		super(MessageType.UNS);

		mSubscriptionId = subscriptionId;
	}


	MessageUns(JSONObject json) throws JSONException {
		super(MessageType.UNS, json);
	}


	@Override
	protected JSONObject createJsonBody() throws JSONException {
		JSONObject body = super.createJsonBody();
		body.put(ATTR_SUB_ID, getSubscriptionId());
		return body;
	}


	@Override
	protected void parseJsonBody(JSONObject jsonBody) {
		super.parseJsonBody(jsonBody);
		mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
	}


	public String getSubscriptionId() {
		return mSubscriptionId;
	}
}

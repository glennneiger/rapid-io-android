package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageUns extends MessageBase {
	private static final String ATTR_SUB_ID = "sub-id";

	private String mSubscriptionId;


	public MessageUns(String eventId, String subscriptionId) {
		super(MessageType.UNS, eventId);

		mSubscriptionId = subscriptionId;
	}


	MessageUns(JSONObject json) throws JSONException {
		super(MessageType.UNS);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_SUB_ID, getSubscriptionId());
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);

		mSubscriptionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_SUB_ID);
	}


	public String getSubscriptionId() {
		return mSubscriptionId;
	}
}

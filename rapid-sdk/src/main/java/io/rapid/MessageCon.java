package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageCon extends MessageBase
{
	private static final String ATTR_CON_ID = "con-id";

	private String mConnectionId;


	public MessageCon(String eventId, String connectionId)
	{
		super(MessageType.CON, eventId);

		mConnectionId = connectionId;
	}


	public MessageCon(JSONObject json)
	{
		super(MessageType.CON);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_CON_ID, getConnectionId());
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json)
	{
		super.fromJson(json);

		mConnectionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_CON_ID);
	}


	public String getConnectionId()
	{
		return mConnectionId;
	}
}

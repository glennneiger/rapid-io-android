package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageAck extends MessageBase
{
	MessageAck(JSONObject json)
	{
		super(MessageType.ACK);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		return super.toJson();
	}


	@Override
	public void fromJson(JSONObject json)
	{
		super.fromJson(json);
	}
}

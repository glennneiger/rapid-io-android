package io.rapid;

import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageUnknown extends MessageBase
{
	MessageUnknown()
	{
		super(MessageType.UNKNOWN);
	}


	@Override
	public JSONObject toJson()
	{
		return null;
	}


	@Override
	public void fromJson(JSONObject json)
	{
	}
}


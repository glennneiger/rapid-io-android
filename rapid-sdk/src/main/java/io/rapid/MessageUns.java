package io.rapid;

import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageUns extends MessageBase
{
	MessageUns(JSONObject json)
	{
		super(MessageType.UNS);
		fromJson(json);
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

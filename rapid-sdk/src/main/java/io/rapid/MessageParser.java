package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageParser
{
	static MessageBase parse(String message) throws JSONException
	{
		JSONObject json = new JSONObject(message);
		String messageType = json.keys().hasNext() ? json.keys().next() : null;

		switch(MessageBase.MessageType.get(messageType))
		{
			case ACK:
				return new MessageAck(json);
			case ERR:
				return new MessageErr(json);
			case MUT:
				return new MessageMut(json);
			case SUB:
				return new MessageSub(json);
			case UNS:
				return new MessageUns(json);
			case UPD:
				return new MessageUpd(json);
			case VAL:
				return new MessageVal(json);
			case UNKNOWN:
				return new MessageUnknown();
			default:
				return new MessageUnknown();
		}
	}
}
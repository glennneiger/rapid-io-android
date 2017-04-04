package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageParser {
	static Message parse(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		String messageType = json.keys().hasNext() ? json.keys().next() : null;

		switch(MessageType.get(messageType)) {
			case ACK:
				return new Message.Ack(json);
			case ERR:
				return new Message.Err(json);
			case MUT:
				return new Message.Mut(json);
			case MER:
				return new Message.Mer(json);
			case SUB:
				return new Message.Sub(json);
			case UNS:
				return new Message.Uns(json);
			case UPD:
				return new Message.Upd(json);
			case VAL:
				return new Message.Val(json);
			case BATCH:
				return new Message.Batch(json);
			case CON:
				return new Message.Con(json);
			case DIS:
				return new Message.Dis(json);
			case NOP:
				return new Message.Nop(json);
			case UNKNOWN:
				return new Message.Unknown();
			default:
				return new Message.Unknown();
		}
	}
}

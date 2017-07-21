package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageParser {
	static Message parse(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		String messageType = json.keys().hasNext() ? json.keys().next().toString() : null;

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
			case MES:
				return new Message.Mes(json);
			case SUB_CH:
				return new Message.SubCh(json);
			case FTC:
				return new Message.Ftc(json);
			case RES:
				return new Message.Res(json);
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
			case CA:
				return new Message.Ca(json);
			case CA_CH:
				return new Message.CaCh(json);
			case PUB:
				return new Message.Pub(json);
			case DEL:
				return new Message.Del(json);
			case RM:
				return new Message.Rm(json);
			case TS:
				return new Message.Ts(json);
			case UNKNOWN:
				return new Message.Unknown();
			default:
				return new Message.Unknown();
		}
	}
}

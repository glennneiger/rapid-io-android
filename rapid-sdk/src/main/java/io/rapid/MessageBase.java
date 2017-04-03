package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

abstract class MessageBase {
	static final String ATTR_EVENT_ID = "evt-id";

	private MessageType mMessageType;
	private String mEventId;

	private Long mSentTimestamp;


	public enum MessageType {
		ACK("ack"), ERR("err"), MUT("mut"), MER("mer"), SUB("sub"), UNS("uns"), VAL("val"),
		UPD("upd"), CON("con"), DIS("dis"), NOP("nop"), BATCH("batch"), UNKNOWN("unw");


		private String mKey;


		MessageType(String key) {
			mKey = key;
		}


		static MessageType get(String key) {
			if(key == null) return UNKNOWN;

			for(MessageType item : MessageType.values()) {
				if(item.getKey().equalsIgnoreCase(key)) {
					return item;
				}
			}
			return UNKNOWN;
		}


		String getKey() {
			return mKey;
		}
	}


	public MessageBase(MessageType messageType) {
		mMessageType = messageType;
	}


	public MessageBase(MessageType messageType, String eventId) {
		mMessageType = messageType;
		mEventId = eventId;
	}


	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(mMessageType.getKey(), new JSONObject().put(ATTR_EVENT_ID, mEventId));
		return json;
	}


	public void fromJson(JSONObject json) throws JSONException {
		if(json != null) mEventId = json.optJSONObject(mMessageType.getKey()).optString(ATTR_EVENT_ID);
	}


	public MessageType getMessageType() {
		return mMessageType;
	}


	public String getEventId() {
		return mEventId;
	}


	public Long getSentTimestamp()
	{
		return mSentTimestamp;
	}


	public void setSentTimestamp(Long sentTimestamp)
	{
		mSentTimestamp = sentTimestamp;
	}
}

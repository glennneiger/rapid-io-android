package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

abstract class MessageBase {
	private static final String ATTR_EVENT_ID = "evt-id";

	private MessageType mMessageType;
	private String mEventId;

	private Long mSentTimestamp;


	public enum MessageType {
		ACK("ack"), ERR("err"), MUT("mut"), MER("mer"), SUB("sub"), UNS("uns"), VAL("val"),
		UPD("upd"), CON("con"), REC("rec"), DIS("dis"), NOP("nop"), BATCH("batch"), UNKNOWN("unw");


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


	public MessageBase(MessageType messageType, JSONObject json) throws JSONException {
		mMessageType = messageType;
		fromJson(json);
	}


	public MessageBase(MessageType messageType) {
		this(messageType, IdProvider.getNewEventId());
	}


	public MessageBase(MessageType messageType, String eventId) {
		mMessageType = messageType;
		mEventId = eventId;
	}


	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		JSONObject body = createJsonBody();
		if(mEventId != null && !mEventId.isEmpty())
			body.put(ATTR_EVENT_ID, mEventId);
		json.put(mMessageType.getKey(), body);
		return json;
	}


	public void fromJson(JSONObject json) throws JSONException {
		if(json != null) {
			mEventId = json.optJSONObject(mMessageType.getKey()).optString(ATTR_EVENT_ID);
			parseJsonBody(json.optJSONObject(mMessageType.getKey()));
		}
	}


	public MessageType getMessageType() {
		return mMessageType;
	}


	public String getEventId() {
		return mEventId;
	}


	public Long getSentTimestamp() {
		return mSentTimestamp;
	}


	public void setSentTimestamp(Long sentTimestamp) {
		mSentTimestamp = sentTimestamp;
	}


	protected JSONObject createJsonBody() throws JSONException {
		return new JSONObject();
	}


	protected void parseJsonBody(JSONObject jsonBody) {

	}
}

package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageSub extends MessageBase
{
	private static final String ATTR_SUB_ID = "sub-id";
	private static final String ATTR_COL_ID = "col-id";

	private String mSubscriptionId;
	private String mCollectionId;


	public MessageSub(String eventId, String collectionId, String subscriptionId)
	{
		super(MessageType.SUB, eventId);

		mCollectionId = collectionId;
		mSubscriptionId = subscriptionId;
	}


	public MessageSub(JSONObject json) throws JSONException
	{
		super(MessageType.SUB);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_SUB_ID, getSubscriptionId());
		innerJson.put(ATTR_COL_ID, getCollectionId());
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException
	{
		super.fromJson(json);

		mSubscriptionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_SUB_ID);
		mCollectionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_COL_ID);
	}


	public String getSubscriptionId()
	{
		return mSubscriptionId;
	}


	public String getCollectionId()
	{
		return mCollectionId;
	}
}

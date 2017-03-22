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
	private static final String ATTR_CON_ID = "con-id";
	private static final String ATTR_LIMIT = "limit";
	private static final String ATTR_SKIP = "skip";

	private String mSubscriptionId;
	private String mCollectionId;
	private String mConnectionId;
	private int mLimit = Config.DEFAULT_LIMIT;
	private int mSkip = 0;


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
		innerJson.put(ATTR_SUB_ID, mSubscriptionId);
		innerJson.put(ATTR_COL_ID, mCollectionId);
		innerJson.put(ATTR_CON_ID, mConnectionId);
		innerJson.put(ATTR_LIMIT, mLimit);
		innerJson.put(ATTR_SKIP, mSkip);

		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException
	{
		super.fromJson(json);

		mSubscriptionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_SUB_ID);
		mCollectionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_COL_ID);
		mConnectionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_CON_ID);
		mLimit = json.optJSONObject(getMessageType().getKey()).optInt(ATTR_LIMIT);
		mSkip = json.optJSONObject(getMessageType().getKey()).optInt(ATTR_SKIP);
	}


	public String getSubscriptionId()
	{
		return mSubscriptionId;
	}


	public String getCollectionId()
	{
		return mCollectionId;
	}


	public String getConnectionId()
	{
		return mConnectionId;
	}


	public void setConnectionId(String connectionId)
	{
		mConnectionId = connectionId;
	}


	public int getLimit()
	{
		return mLimit;
	}


	public void setLimit(int limit)
	{
		mLimit = limit;
	}


	public int getSkip()
	{
		return mSkip;
	}


	public void setSkip(int skip)
	{
		mSkip = skip;
	}
}

package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageVal extends MessageBase
{
	private static final String ATTR_SUB_ID = "sub-id";
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_CON_ID = "con-id";
	private static final String ATTR_DOCS = "docs";

	private String mSubscriptionId;
	private String mCollectionId;
	private String mConnectionId;
	private String mDocuments;


	MessageVal(JSONObject json) throws JSONException
	{
		super(MessageType.VAL);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_SUB_ID, getSubscriptionId());
		innerJson.put(ATTR_COL_ID, getCollectionId());
		innerJson.put(ATTR_CON_ID, getConnectionId());
		innerJson.put(ATTR_DOCS, getDocuments());
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
		mDocuments = json.optJSONObject(getMessageType().getKey()).optString(ATTR_DOCS);
	}


	public String getSubscriptionId()
	{
		return mSubscriptionId;
	}


	public String getCollectionId()
	{
		return mCollectionId;
	}


	public String getDocuments()
	{
		return mDocuments;
	}


	public String getConnectionId()
	{
		return mConnectionId;
	}
}

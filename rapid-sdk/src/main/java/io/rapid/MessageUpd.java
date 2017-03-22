package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageUpd extends MessageBase
{
	private static final String ATTR_SUB_ID = "sub-id";
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_DOC = "doc";

	private String mSubscriptionId;
	private String mCollectionId;
	private String mDocument;


	MessageUpd(JSONObject json) throws JSONException
	{
		super(MessageType.UPD);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_SUB_ID, getSubscriptionId());
		innerJson.put(ATTR_COL_ID, getCollectionId());
		innerJson.put(ATTR_DOC, getDocument());
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException
	{
		super.fromJson(json);

		mSubscriptionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_SUB_ID);
		mCollectionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_COL_ID);
		mDocument = json.optJSONObject(getMessageType().getKey()).optString(ATTR_DOC);
	}


	public String getSubscriptionId()
	{
		return mSubscriptionId;
	}


	public String getCollectionId()
	{
		return mCollectionId;
	}


	public String getDocument()
	{
		return mDocument;
	}
}

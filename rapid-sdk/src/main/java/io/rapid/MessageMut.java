package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageMut extends MessageBase
{
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_DOC = "doc";

	private String mCollectionId;
	private String mDocument;


	public MessageMut(JSONObject json)
	{
		super(MessageType.MUT);
		fromJson(json);
	}


	public MessageMut(String eventId, String collectionId, String document)
	{
		super(MessageType.MUT, eventId);
		mCollectionId = collectionId;
		mDocument = document;
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_COL_ID, getCollectionId());
		innerJson.put(ATTR_DOC, new JSONObject(getDocument()));
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json)
	{
		super.fromJson(json);

		mCollectionId = json.optJSONObject(getMessageType().getKey()).optString(ATTR_COL_ID);
		mDocument = json.optJSONObject(getMessageType().getKey()).optString(ATTR_DOC);
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

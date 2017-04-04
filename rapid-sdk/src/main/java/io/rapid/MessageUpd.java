package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageUpd extends MessageBase {
	private static final String ATTR_SUB_ID = "sub-id";
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_DOC = "doc";

	private String mSubscriptionId;
	private String mCollectionId;
	private String mDocument;


	MessageUpd(JSONObject json) throws JSONException {
		super(MessageType.UPD, json);
	}


	@Override
	protected JSONObject createJsonBody() throws JSONException {
		JSONObject body = super.createJsonBody();
		body.put(ATTR_SUB_ID, getSubscriptionId());
		body.put(ATTR_COL_ID, getCollectionId());
		body.put(ATTR_DOC, getDocument());
		return body;
	}


	@Override
	protected void parseJsonBody(JSONObject jsonBody) {
		super.parseJsonBody(jsonBody);

		mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
		mCollectionId = jsonBody.optString(ATTR_COL_ID);
		mDocument = jsonBody.optString(ATTR_DOC);
	}


	public String getSubscriptionId() {
		return mSubscriptionId;
	}


	public String getCollectionId() {
		return mCollectionId;
	}


	public String getDocument() {
		return mDocument;
	}
}

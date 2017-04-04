package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageMut extends MessageBase {
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_DOC = "doc";

	private String mCollectionId;
	private String mDocument;


	public MessageMut(JSONObject json) throws JSONException {
		super(MessageType.MUT, json);
	}


	public MessageMut(String collectionId, String document) {
		super(MessageType.MUT);
		mCollectionId = collectionId;
		mDocument = document;
	}


	@Override
	protected JSONObject createJsonBody() throws JSONException {
		JSONObject body = super.createJsonBody();
		body.put(ATTR_COL_ID, getCollectionId());
		body.put(ATTR_DOC, new JSONObject(getDocument()));
		return body;
	}


	@Override
	protected void parseJsonBody(JSONObject jsonBody) {
		super.parseJsonBody(jsonBody);
		mCollectionId = jsonBody.optString(ATTR_COL_ID);
		mDocument = jsonBody.optString(ATTR_DOC);
	}


	public String getCollectionId() {
		return mCollectionId;
	}


	public String getDocument() {
		return mDocument;
	}
}

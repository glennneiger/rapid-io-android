package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageMer extends MessageBase {
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_DOC = "doc";

	private String mCollectionId;
	private String mDocument;


	public MessageMer(JSONObject json) throws JSONException {
		super(MessageType.MER, json);
	}


	public MessageMer(String collectionId, String document) {
		super(MessageType.MER);
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

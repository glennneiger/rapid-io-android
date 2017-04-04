package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageErr extends MessageBase {
	private static final String ATTR_ERR_TYPE = "err-type";
	private static final String ATTR_ERR_MSG = "err-msg";

	private ErrorType mErrorType;
	private String mErrorMessage;


	enum ErrorType {
		INTERNAL_ERROR("internal-error"), PERMISSION_DENIED("permission-denied"), CONNECTION_TERMINATED("connection-terminated");


		private String mKey;


		ErrorType(String key) {
			mKey = key;
		}


		static ErrorType get(String key) {
			if(key == null) return INTERNAL_ERROR;

			for(ErrorType item : ErrorType.values()) {
				if(item.getKey().equalsIgnoreCase(key)) {
					return item;
				}
			}
			return INTERNAL_ERROR;
		}


		String getKey() {
			return mKey;
		}
	}


	MessageErr(ErrorType errorType, String errorMessage) throws JSONException {
		super(MessageType.ERR);
		mErrorType = errorType;
		mErrorMessage = errorMessage;
	}


	MessageErr(JSONObject json) throws JSONException {
		super(MessageType.ERR, json);
	}


	@Override
	protected JSONObject createJsonBody() throws JSONException {
		JSONObject body = super.createJsonBody();
		body.put(ATTR_ERR_TYPE, getErrorType().getKey());
		body.put(ATTR_ERR_MSG, getErrorMessage());
		return body;
	}


	@Override
	protected void parseJsonBody(JSONObject jsonBody) {
		super.parseJsonBody(jsonBody);
		mErrorType = ErrorType.get(jsonBody.optString(ATTR_ERR_TYPE));
		mErrorMessage = jsonBody.optString(ATTR_ERR_MSG);
	}


	public ErrorType getErrorType() {
		return mErrorType;
	}


	public String getErrorMessage() {
		return mErrorMessage;
	}
}

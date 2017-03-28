package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Leos on 17.03.2017.
 */

class MessageErr extends MessageBase
{
	private static final String ATTR_ERR_TYPE = "err-type";
	private static final String ATTR_ERR_MSG = "err-msg";

	private ErrorType mErrorType;
	private String mErrorMessage;


	enum ErrorType
	{
		INTERNAL_ERROR("internal-error"), PERMISSION_DENIED("permission-denied"), CONNECTION_TERMINATED("connection-terminated");


		private String mKey;


		static ErrorType get(String key)
		{
			if(key == null) return INTERNAL_ERROR;

			for(ErrorType item : ErrorType.values())
			{
				if(item.getKey().equalsIgnoreCase(key))
				{
					return item;
				}
			}
			return INTERNAL_ERROR;
		}


		ErrorType(String key)
		{
			mKey = key;
		}


		String getKey()
		{
			return mKey;
		}
	}


	MessageErr(String eventId, ErrorType errorType, String errorMessage) throws JSONException
	{
		super(MessageType.ERR, eventId);
		mErrorType = errorType;
		mErrorMessage = errorMessage;
	}


	MessageErr(JSONObject json) throws JSONException
	{
		super(MessageType.ERR);
		fromJson(json);
	}


	@Override
	public JSONObject toJson() throws JSONException
	{
		JSONObject json = super.toJson();
		JSONObject innerJson = json.optJSONObject(getMessageType().getKey());
		innerJson.put(ATTR_ERR_TYPE, getErrorType().getKey());
		innerJson.put(ATTR_ERR_MSG, getErrorMessage());
		json.put(getMessageType().getKey(), innerJson);
		return json;
	}


	@Override
	public void fromJson(JSONObject json) throws JSONException
	{
		super.fromJson(json);

		mErrorType = ErrorType.get(json.optJSONObject(getMessageType().getKey()).optString(ATTR_ERR_TYPE));
		mErrorMessage = json.optJSONObject(getMessageType().getKey()).optString(ATTR_ERR_MSG);
	}


	public ErrorType getErrorType()
	{
		return mErrorType;
	}


	public String getErrorMessage()
	{
		return mErrorMessage;
	}
}

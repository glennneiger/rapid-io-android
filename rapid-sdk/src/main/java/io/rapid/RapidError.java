package io.rapid;


public class RapidError extends Error {

	public enum ErrorType
	{
		TIMEOUT("Timeout", "Connection timed out"),
		SUBSCRIPTION_CANCELED("Subscription canceled", ""),
		INTERNAL_ERROR("Internal server error", ""),
		PERMISSION_DENIED("Permission denied", ""),
		CONNECTION_TERMINATED("Connection terminated", ""),
		INVALID_AUTH_TOKEN("Invalid Auth Token", "");

		private String mName;
		private String mMessage;


		ErrorType(String name, String message) {
			mName = name;
			mMessage = message;
		}


		public String getName()
		{
			return mName;
		}


		public String getMessage()
		{
			return mMessage;
		}
	}

	private ErrorType mType;



	public RapidError(ErrorType type) {
		super("Rapid Error - " + type);
		mType = type;
	}


	public RapidError(Message.Err message)
	{
		super("Rapid Error - " + message);

		switch(message.getErrorType())
		{
			case CONNECTION_TERMINATED:
				mType = ErrorType.CONNECTION_TERMINATED;
				break;
			case INTERNAL_ERROR:
				mType = ErrorType.INTERNAL_ERROR;
				break;
			case INVALID_AUTH_TOKEN:
				mType = ErrorType.INVALID_AUTH_TOKEN;
				break;
			case PERMISSION_DENIED:
				mType = ErrorType.PERMISSION_DENIED;
				break;
		}
	}


	public ErrorType getType()
	{
		return mType;
	}
}

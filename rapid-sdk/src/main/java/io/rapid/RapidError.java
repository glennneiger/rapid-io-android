package io.rapid;


public class RapidError extends Error {

	public enum ErrorType
	{
		TIMEOUT("Timeout", "Connection timed out"),
		SUBSCRIPTION_CANCELED("Subscription canceled", ""),
		INTERNAL_ERROR("Internal server error", ""),
		PERMISSION_DENIED("Permission denied", ""),
		CONNECTION_TERMINATED("Connection terminated", ""),
		INVALID_AUTH_TOKEN("Invalid Auth Token", ""),
		UNKNOWN_ERROR("Unknown Error", "");

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
		super("Rapid Error: " + type.getName());
		mType = type;
	}



	public ErrorType getType()
	{
		return mType;
	}
}

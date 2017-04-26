package io.rapid;


public class RapidError extends Error {

	public enum ErrorType
	{
		PERMISSION_DENIED("permission-denied"), TIMEOUT("timeout"), INTERNAL_SERVER_ERROR("internal-server-error"),
		SUBSCRIPTION_CANCELED("subscription-canceled"), INVALID_AUTH_TOKEN("invalid-auth-token");

		private String mKey;


		ErrorType(String key) {
			mKey = key;
		}


		public String getKey()
		{
			return mKey;
		}


		static ErrorType get(String key) {
			if(key == null) return INTERNAL_SERVER_ERROR;

			for(ErrorType item : ErrorType.values()) {
				if(item.getKey().equalsIgnoreCase(key)) {
					return item;
				}
			}
			return INTERNAL_SERVER_ERROR;
		}
	}

	private ErrorType mType;


	public RapidError(ErrorType type) {
		super("Rapid Error - " + type);
		mType = type;
	}


	public ErrorType getType() {
		return mType;
	}
}

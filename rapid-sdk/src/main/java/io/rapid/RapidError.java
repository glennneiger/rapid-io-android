package io.rapid;


public class RapidError extends Error {
	public static final String PERMISSION_DENIED = "permission-denied";
	public static final String TIMEOUT = "timeout";

	private String mType;


	public RapidError(String type) {
		super("Rapid Error - " + type);
		mType = type;
	}


	public String getType() {
		return mType;
	}
}

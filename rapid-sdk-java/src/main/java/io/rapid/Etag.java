package io.rapid;


import org.json.JSONObject;


public class Etag {
	public static final Etag ANY_ETAG = fromValue(null);
	public static final Etag NO_ETAG = fromValue("#__null__#");
	private String mValue;


	private Etag(String value) {
		mValue = value;
	}


	public static Etag fromValue(String value) {
		return new Etag(value);
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Etag)
			return mValue.equals(((Etag) obj).mValue);
		else return super.equals(obj);
	}


	Object getSerialized() {
		if(NO_ETAG.mValue.equals(mValue)) return JSONObject.NULL;
		else return mValue;
	}
}

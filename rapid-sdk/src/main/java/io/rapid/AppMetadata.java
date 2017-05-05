package io.rapid;


import android.util.Base64;


class AppMetadata {

	private final String mUrl;


	AppMetadata(String apiKey) {
		mUrl = "ws://" + new String(Base64.decode(apiKey, Base64.DEFAULT));
	}


	String getUrl() {
		return mUrl;
	}
}

package io.rapid;


import android.util.Base64;


class AppMetadata {

	private final String mUrl;


	public AppMetadata(String apiKey) {
		mUrl = "ws://" + new String(Base64.decode(apiKey, Base64.DEFAULT));
	}


	public String getUrl() {
		return mUrl;
	}
}

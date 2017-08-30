package io.rapid;


import android.support.annotation.NonNull;
import android.util.Base64;


class AppMetadata {

	@NonNull private final String mUrl;


	AppMetadata(String apiKey) {
		mUrl = "wss://" + new String(Base64.decode(apiKey, Base64.DEFAULT));
	}


	@NonNull
	String getUrl() {
		return mUrl;
	}
}

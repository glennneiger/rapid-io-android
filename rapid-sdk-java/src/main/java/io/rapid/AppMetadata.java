package io.rapid;


import org.java_websocket.util.Base64;

import java.io.IOException;


class AppMetadata {

	private final String mUrl;


	AppMetadata(String apiKey) {
		try {
			mUrl = "wss://" + new String(Base64.decode(apiKey));
		} catch(IOException e) {
			throw new IllegalArgumentException("Invalid API Key", e);
		}
	}


	String getUrl() {
		return mUrl;
	}
}

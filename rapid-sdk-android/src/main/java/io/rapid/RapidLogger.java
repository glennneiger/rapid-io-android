package io.rapid;


import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class RapidLogger {

	private static final String TAG = "Rapid.io";

	private static final int TYPE_ERROR = 1;
	private static final int TYPE_WARNING = 2;
	private static final int TYPE_INFO = 3;
	private static final int TYPE_VERBOSE = 4;

	private int mLevel;


	public void logW(@NonNull String message, Object... args) {
		log(TYPE_WARNING, message, null, args);
	}


	void setLevel(@LogLevel int level) {
		mLevel = level;
	}


	void logE(@NonNull String message, Object... args) {
		log(TYPE_ERROR, message, null, args);
	}


	void logE(Throwable throwable, Object... args) {
		log(TYPE_ERROR, "", throwable, args);
	}


	void logI(@NonNull String message, Object... args) {
		log(TYPE_INFO, message, null, args);
	}


	void logJson(@NonNull String json) {
		logJson(TYPE_VERBOSE, json);
	}


	private void logJson(int type, @NonNull String json) {
		if(type > mLevel) return;
		try {
			String message;
			if(json.startsWith("["))
				message = new JSONArray(json).toString(4);
			else
				message = new JSONObject(json).toString(4);
			log(type, message, null);
		} catch(JSONException e) {
			e.printStackTrace();
		}

	}


	private void log(int type, @NonNull String message, Throwable throwable, Object... args) {
		if(type > mLevel) return;
		String formattedMessage;
		try {
			formattedMessage = String.format(message, args);
		} catch(Exception e) {
			formattedMessage = message;
		}
		switch(type) {
			case TYPE_ERROR:
				Log.e(TAG, formattedMessage, throwable);
				break;
			case TYPE_WARNING:
				Log.w(TAG, formattedMessage, throwable);
				break;
			default:
				Log.i(TAG, formattedMessage, throwable);
				break;
		}
	}

}

package io.rapid;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


class RapidLogger {

	private static final String TAG = "Rapid.io";

	private static final int TYPE_ERROR = 1;
	private static final int TYPE_WARNING = 2;
	private static final int TYPE_INFO = 3;

	private int mLevel;


	public void setLevel(@LogLevel int level) {
		mLevel = level;
	}


	public void logE(String message, Object... args) {
		log(TYPE_ERROR, message, null, args);
	}


	public void logE(Throwable throwable, Object... args) {
		log(TYPE_ERROR, "", throwable, args);
	}


	public void logW(String message, Object... args) {
		log(TYPE_WARNING, message, null, args);
	}


	public void logI(String message, Object... args) {
		log(TYPE_INFO, message, null, args);
	}


	public void logJson(String json) {
		logJson(TYPE_INFO, json);
	}


	private void logJson(int type, String json) {
		if(type > mLevel) return;
		try {
			String message = new JSONObject(json).toString(4);
			log(type, message, null);
		} catch(JSONException e) {
			e.printStackTrace();
		}

	}


	private void log(int type, String message, Throwable throwable, Object... args) {
		if(type > mLevel) return;
		String formattedMessage = String.format(message, args);
		switch(type) {
			case TYPE_ERROR:
				Log.e(TAG, formattedMessage, throwable);
				break;
			case TYPE_WARNING:
				Log.w(TAG, formattedMessage, throwable);
				break;
			case TYPE_INFO:
				Log.i(TAG, formattedMessage, throwable);
				break;
		}
	}

}

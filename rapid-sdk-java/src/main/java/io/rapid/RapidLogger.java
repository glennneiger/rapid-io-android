package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RapidLogger {
	private static final int TYPE_ERROR = 1;
	private static final int TYPE_WARNING = 2;
	private static final int TYPE_INFO = 3;
	private static final int TYPE_VERBOSE = 4;

	private int mLevel;


	private LoggerOutput mOutput;


	public RapidLogger(LoggerOutput output) {
		mOutput = output;
	}


	void setLevel(int level) {
		mLevel = level;
	}


	void logE(String message, Object... args) {
		log(TYPE_ERROR, message, null, args);
	}


	void logE(Throwable throwable, Object... args) {
		log(TYPE_ERROR, "", throwable, args);
	}


	public void logW(String message, Object... args) {
		log(TYPE_WARNING, message, null, args);
	}


	void logI(String message, Object... args) {
		log(TYPE_INFO, message, null, args);
	}


	void logJson(String json) {
		logJson(TYPE_VERBOSE, json);
	}


	private void logJson(int type, String json) {
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


	private void log(int type, String message, Throwable throwable, Object... args) {
		if(type > mLevel) return;
		String formattedMessage;
		try {
			formattedMessage = String.format(message, args);
		} catch(Exception e) {
			formattedMessage = message;
		}
		switch(type) {
			case TYPE_ERROR:
				mOutput.error(formattedMessage, throwable);
				break;
			case TYPE_WARNING:
				mOutput.warning(formattedMessage, throwable);
				break;
			default:
				mOutput.info(formattedMessage, throwable);
				break;
		}
	}

}

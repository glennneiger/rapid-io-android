package io.rapid;

import android.text.format.DateUtils;


/**
 * Created by Leos on 16.03.2017.
 */

class Config {
	public static final boolean LOGS = true;
	public static final String URI = "ws://13.64.77.202:8080";
	public static final int DEFAULT_LIMIT = 100;
	public static final String ID_IDENTIFIER = "$id";
	public static final int MESSAGE_TIMEOUT = 30 * 1000;
	public static final long HB_PERIOD = 10 * DateUtils.SECOND_IN_MILLIS;
}

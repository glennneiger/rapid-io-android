package io.rapid;

import android.text.format.DateUtils;


class Config {
	static final int DEFAULT_LIMIT = 100;
	static final long DEFAULT_CONNECTION_TIMEOUT = Long.MAX_VALUE;
	static final long CONNECTION_RETRY_PERIOD = 5 * DateUtils.SECOND_IN_MILLIS;
	static final long HB_PERIOD = 10 * DateUtils.SECOND_IN_MILLIS;
	static final int CHECKER_HANDLER_PERIOD = 10 * 1000;
	static final String API_KEY_METADATA = "io.rapid.apikey";
	static final int CACHE_DEFAULT_SIZE_MB = 50;
	static final long WEBSOCKET_DISCONNECT_TIMEOUT = 30 * DateUtils.SECOND_IN_MILLIS;
}

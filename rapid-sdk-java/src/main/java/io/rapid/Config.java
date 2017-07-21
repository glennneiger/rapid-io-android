package io.rapid;

class Config {
	static final int DEFAULT_LIMIT = 100;
	static final String ID_IDENTIFIER = "$id";
	static final long DEFAULT_CONNECTION_TIMEOUT = Long.MAX_VALUE;
	static final long CONNECTION_RETRY_PERIOD = 5 * 1000;
	static final long HB_PERIOD = 10 * 1000;
	static final int CHECKER_HANDLER_PERIOD = 10 * 1000;
	static final String API_KEY_METADATA = "io.rapid.apikey";
	static final int CACHE_DEFAULT_SIZE_MB = 50;
	static final long WEBSOCKET_DISCONNECT_TIMEOUT = 30 * 1000;
}

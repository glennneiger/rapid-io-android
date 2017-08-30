package io.rapid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


enum CloseReason {
	UNKNOWN, INTERNET_CONNECTION_LOST, NO_INTERNET_CONNECTION, CLOSED_MANUALLY, CLOSED_FROM_SERVER, HANDSHAKE_ERROR;


	// for lib connection
	@NonNull
	static CloseReason get(int code) {
		switch(code) {
			case 1006:
				return INTERNET_CONNECTION_LOST;
			case -1:
				return NO_INTERNET_CONNECTION;
			case 1000:
				return CLOSED_MANUALLY;
			default:
				return UNKNOWN;
		}
	}


	// for Async connection
	@NonNull
	static CloseReason get(@Nullable Exception ex) {
		if(ex == null) {
			return CLOSED_FROM_SERVER;
		} else if(ex.getMessage() == null) {
			return UNKNOWN;
		} else {
			switch(ex.getMessage()) {
				case "Software caused connection abort":
					return INTERNET_CONNECTION_LOST;
				case "Unable to complete websocket handshake":
					return HANDSHAKE_ERROR;
				default:
					return UNKNOWN;
			}
		}
	}
}
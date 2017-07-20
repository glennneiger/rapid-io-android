package io.rapid;


import java.util.UUID;

import io.rapid.utility.UUIDUtility;
import me.nimavat.shortid.ShortId;


class IdProvider {
	static String getNewEventId() {
		return ShortId.generate();
	}


	static String getNewSubscriptionId() {
		return ShortId.generate();
	}


	static String getNewDocumentId() {
		return UUIDUtility.base64(UUID.randomUUID());
	}


	static String getConnectionId() {
		return ShortId.generate();
	}


	static String getNewActionId() {
		return ShortId.generate();
	}
}

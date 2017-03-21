package io.rapid;


import java.util.UUID;


class IdProvider {
	public static String getNewEventId() {
		return UUID.randomUUID().toString();
	}


	public static String getNewSubscriptionId() {
		return UUID.randomUUID().toString();
	}


	static String getNewDocumentId() {
		return UUID.randomUUID().toString();
	}


	static String getConnectionId() {
		return UUID.randomUUID().toString();
	}
}

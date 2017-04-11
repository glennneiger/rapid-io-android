package io.rapid;


import me.nimavat.shortid.ShortId;


class IdProvider {
	public static String getNewEventId() {
		return ShortId.generate();
	}


	public static String getNewSubscriptionId() {
		return ShortId.generate();
	}


	static String getNewDocumentId() {
		return ShortId.generate();
	}


	static String getConnectionId() {
		return ShortId.generate();
	}
}

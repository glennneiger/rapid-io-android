package io.rapid;


import me.nimavat.shortid.ShortId;


class IdProvider {
	static String getNewEventId() {
		return ShortId.generate();
	}


	static String getNewSubscriptionId() {
		return ShortId.generate();
	}


	static String getNewDocumentId() {
		return ShortId.generate();
	}


	static String getConnectionId() {
		return ShortId.generate();
	}
}

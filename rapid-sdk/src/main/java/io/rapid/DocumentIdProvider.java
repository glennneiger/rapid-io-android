package io.rapid;


import java.util.UUID;


class DocumentIdProvider {
	static String getNewId() {
		return UUID.randomUUID().toString();
	}
}

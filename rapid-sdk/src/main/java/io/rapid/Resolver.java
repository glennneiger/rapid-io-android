package io.rapid;


class Resolver {
	interface Message<T extends io.rapid.Message> {
		T getMessage();
	}


	interface String {
		java.lang.String getString();
	}
}

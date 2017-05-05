package io.rapid;


enum MessageType {
	ACK("ack"), ERR("err"), AUTH("auth"), DEAUTH("deauth"), MUT("mut"), MER("mer"), SUB("sub"), UNS("uns"), VAL("val"),
	UPD("upd"), CON("con"), REC("rec"), DIS("dis"), NOP("nop"), BATCH("batch"), CA("ca"), UNKNOWN("unw"), DEL("del"), RM("rm");


	private String mKey;


	MessageType(String key) {
		mKey = key;
	}


	static MessageType get(String key) {
		if(key == null) return UNKNOWN;

		for(MessageType item : MessageType.values()) {
			if(item.getKey().equalsIgnoreCase(key)) {
				return item;
			}
		}
		return UNKNOWN;
	}


	String getKey() {
		return mKey;
	}
}

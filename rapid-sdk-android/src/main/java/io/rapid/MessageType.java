package io.rapid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


enum MessageType {
	ACK("ack"), ERR("err"), AUTH("auth"), DEAUTH("deauth"), MUT("mut"), MER("mer"), SUB("sub"), UNS("uns"), VAL("val"),
	UPD("upd"), CON("con"), REC("rec"), DIS("dis"), NOP("nop"), BATCH("batch"), CA("ca"), UNKNOWN("unw"), DEL("del"),
	RM("rm"), FTC("ftc"), RES("res"), SUB_CH("sub-ch"), MES("mes"), CA_CH("ca-ch"), PUB("pub"), UNS_CH("uns-ch"), REQ_TS("req-ts"), TS("ts"), DA("da"), DA_CA("da-ca"), CA_DA("ca-da");


	private String mKey;


	MessageType(String key) {
		mKey = key;
	}


	@NonNull
	static MessageType get(@Nullable String key) {
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

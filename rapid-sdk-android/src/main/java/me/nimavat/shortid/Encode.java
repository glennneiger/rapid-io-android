package me.nimavat.shortid;

import android.support.annotation.NonNull;

import java.util.function.Function;


class Encode {

	@NonNull
	static String encode(@NonNull Function<Integer, Character> lookup, int number) {
		int loopCounter = 0;
		boolean done = false;

		String str = "";

		while(!done) {
			str = str + lookup.apply(((number >> (4 * loopCounter)) & 0x0f) | randomByte());
			done = number < (Math.pow(16, loopCounter + 1));
			loopCounter++;
		}

		return str;
	}


	static int randomByte() {
		return ((int) Math.floor(Math.random() * 256)) & 0x30;
	}

}

package me.nimavat.shortid;


import com.annimon.stream.function.Function;


class Encode {

	static String encode(Function<Integer, Character> lookup, int number) {
		int loopCounter = 0;
		boolean done = false;

		String str = "";

		while (!done) {
			str = str + lookup.apply( ( (number >> (4 * loopCounter)) & 0x0f ) | randomByte() );
			done = number < (Math.pow(16, loopCounter + 1 ) );
			loopCounter++;
		}

		return str;
	}

	static int randomByte() {
		return ((int)Math.floor(Math.random() * 256)) & 0x30;
	}

}

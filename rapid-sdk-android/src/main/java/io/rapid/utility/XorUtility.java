package io.rapid.utility;


import android.support.annotation.NonNull;


public class XorUtility
{
	public static byte[] xor(@NonNull String input, String key) {
		if(key == null) {
			return input.getBytes();
		}
		else {
			return xor(input.getBytes(), key.getBytes());
		}
	}


	public static String xor(@NonNull byte[] input, String key) {
		if(key == null) {
			return new String(input);
		}
		else {
			return new String(xor(input, key.getBytes()));
		}
	}


	private static byte[] xor(@NonNull byte[] input, byte[] key) {
		byte[] outputArray = new byte[input.length];

		for (int i = 0; i < input.length; i++) {
			outputArray[i] = (byte) (input[i] ^ key[i%key.length]);
		}
		return outputArray;
	}
}

package io.rapid.utility;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamUtility
{
	public static byte[] toByteArray(InputStream in) throws IOException {
		// Presize the ByteArrayOutputStream since we know how large it will need
		// to be, unless that value is less than the default ByteArrayOutputStream
		// size (32).
		ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(32, in.available()));
		copy(in, out);
		return out.toByteArray();
	}

	public static long copy(InputStream from, OutputStream to) throws IOException
	{
		byte[] buf = createBuffer();
		long total = 0;
		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				break;
			}
			to.write(buf, 0, r);
			total += r;
		}
		return total;
	}


	static byte[] createBuffer() {
		return new byte[8192];
	}

}

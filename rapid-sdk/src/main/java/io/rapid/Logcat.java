package io.rapid;

import android.util.Log;


/**
 * Created by Leos on 16.03.2017.
 */

class Logcat {
	public static final String TAG = "Rapid.IO";

	private static final boolean SHOW_CODE_LOCATION = !BuildConfig.RELEASE;
	private static final boolean SHOW_CODE_LOCATION_THREAD = false;
	private static final boolean SHOW_CODE_LOCATION_LINE = !BuildConfig.RELEASE;


	private Logcat() {}


	public static void d(String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.d(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void pd(String msg, Object... args) {
		Log.d(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void e(String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.e(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void pe(String msg, Object... args) {
		Log.e(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void e(Throwable throwable, String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.e(TAG, getCodeLocation().toString() + formatMessage(msg, args), throwable);
	}


	public static void pe(Throwable throwable, String msg, Object... args) {
		Log.e(TAG, getCodeLocation().toString() + formatMessage(msg, args), throwable);
	}


	public static void i(String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.i(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void pi(String msg, Object... args) {
		Log.i(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void v(String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.v(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void pv(String msg, Object... args) {
		Log.v(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void w(String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.w(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void pw(String msg, Object... args) {
		Log.w(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void wtf(String msg, Object... args) {
		if(!BuildConfig.RELEASE) Log.wtf(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void pwtf(String msg, Object... args) {
		Log.wtf(TAG, getCodeLocation().toString() + formatMessage(msg, args));
	}


	public static void printStackTrace(Throwable throwable) {
		e(throwable, "");
	}


	private static String formatMessage(String msg, Object... args) {
		return args.length == 0 ? msg : String.format(msg, args);
	}


	private static CodeLocation getCodeLocation() {
		return getCodeLocation(3);
	}


	private static CodeLocation getCodeLocation(int depth) {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		StackTraceElement[] filteredStackTrace = new StackTraceElement[stackTrace.length - depth];
		System.arraycopy(stackTrace, depth, filteredStackTrace, 0, filteredStackTrace.length);
		return new CodeLocation(filteredStackTrace);
	}


	private static class CodeLocation {
		private final String mThread;
		private final String mFileName;
		private final String mClassName;
		private final String mMethod;
		private final int mLineNumber;


		CodeLocation(StackTraceElement[] stackTrace) {
			StackTraceElement root = stackTrace[0];
			mThread = Thread.currentThread().getName();
			mFileName = root.getFileName();
			String className = root.getClassName();
			mClassName = className.substring(className.lastIndexOf('.') + 1);
			mMethod = root.getMethodName();
			mLineNumber = root.getLineNumber();
		}


		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if(SHOW_CODE_LOCATION) {
				builder.append('[');
				if(SHOW_CODE_LOCATION_THREAD) {
					builder.append(mThread);
					builder.append('.');
				}
				builder.append(mClassName);
				builder.append('.');
				builder.append(mMethod);
				if(SHOW_CODE_LOCATION_LINE) {
					builder.append('(');
					builder.append(mFileName);
					builder.append(':');
					builder.append(mLineNumber);
					builder.append(')');
				}
				builder.append("] ");
			}
			return builder.toString();
		}
	}
}

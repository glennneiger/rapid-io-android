package io.rapid;


import android.os.Handler;
import android.os.Looper;

import io.rapid.converter.RapidGsonConverter;
import io.rapid.converter.RapidJsonConverter;
import io.rapid.executor.AndroidRapidExecutor;
import io.rapid.executor.RapidExecutor;


public class RapidConfig {

	private @LogLevel int mLogLevel = LogLevel.LOG_LEVEL_INFO;
	private RapidJsonConverter mJsonConverter = new RapidGsonConverter();
	private RapidExecutor mExecutor = new AndroidRapidExecutor(new Handler(Looper.getMainLooper()));
	private int mCacheSize = Config.CACHE_DEFAULT_SIZE_MB;


	public int getLogLevel() {
		return mLogLevel;
	}


	public RapidJsonConverter getJsonConverter() {
		return mJsonConverter;
	}


	public RapidExecutor getExecutor() {
		return mExecutor;
	}


	public int getCacheSize() {
		return mCacheSize;
	}


	private RapidConfig() {
	}


	public static class Builder {
		RapidConfig mConfig = new RapidConfig();


		/**
		 * Set level of Logcat output
		 * <p>
		 * {@link LogLevel#LOG_LEVEL_NONE} - no logs at all
		 * <p>
		 * {@link LogLevel#LOG_LEVEL_ERRORS} - log only errors
		 * <p>
		 * {@link LogLevel#LOG_LEVEL_WARNINGS} - log errors and warnings
		 * <p>
		 * {@link LogLevel#LOG_LEVEL_INFO} - log errors, warnings and informative messages useful for debugging
		 * <p>
		 * {@link LogLevel#LOG_LEVEL_VERBOSE} - log everything
		 *
		 * @param level desired log level
		 */
		public Builder setLogLevel(@LogLevel int level) {
			mConfig.mLogLevel = level;
			return this;
		}


		/**
		 * Set JSON converter used for serialization and deserialization to/from Java objects.
		 * <p>
		 * By default, Gson is used for this task
		 *
		 * @param jsonConverter Custom JSON converter
		 */
		public Builder setJsonConverter(RapidJsonConverter jsonConverter) {
			mConfig.mJsonConverter = jsonConverter;
			return this;
		}


		public Builder setExecutor(RapidExecutor executor) {
			mConfig.mExecutor = executor;
			return this;
		}


		/**
		 * Adjust subscription disk cache size
		 * <p>
		 * Default size is 50 MB
		 *
		 * @param cacheSizeInMb Cache size in MB
		 */
		public Builder setCacheSize(int cacheSizeInMb) {
			mConfig.mCacheSize = cacheSizeInMb;
			return this;
		}


		public RapidConfig build() {
			return mConfig;
		}
	}
}

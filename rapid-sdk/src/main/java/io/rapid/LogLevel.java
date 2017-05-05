package io.rapid;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({
		LogLevel.LOG_LEVEL_NONE,
		LogLevel.LOG_LEVEL_ERRORS,
		LogLevel.LOG_LEVEL_WARNINGS,
		LogLevel.LOG_LEVEL_INFO,
		LogLevel.LOG_LEVEL_VERBOSE
})
@Retention(RetentionPolicy.SOURCE)
public @interface LogLevel {
	int LOG_LEVEL_NONE = 0;
	int LOG_LEVEL_ERRORS = 1;
	int LOG_LEVEL_WARNINGS = 2;
	int LOG_LEVEL_INFO = 3;
	int LOG_LEVEL_VERBOSE = 4;
}

package io.rapid;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static io.rapid.LogLevel.LOG_LEVEL_ERRORS;
import static io.rapid.LogLevel.LOG_LEVEL_INFO;
import static io.rapid.LogLevel.LOG_LEVEL_NONE;
import static io.rapid.LogLevel.LOG_LEVEL_VERBOSE;
import static io.rapid.LogLevel.LOG_LEVEL_WARNINGS;


@IntDef({LOG_LEVEL_NONE, LOG_LEVEL_ERRORS, LOG_LEVEL_WARNINGS, LOG_LEVEL_INFO, LOG_LEVEL_VERBOSE})
@Retention(RetentionPolicy.SOURCE)
public @interface LogLevel {
	int LOG_LEVEL_NONE = 0;
	int LOG_LEVEL_ERRORS = 1;
	int LOG_LEVEL_WARNINGS = 2;
	int LOG_LEVEL_INFO = 3;
	int LOG_LEVEL_VERBOSE = 4;
}

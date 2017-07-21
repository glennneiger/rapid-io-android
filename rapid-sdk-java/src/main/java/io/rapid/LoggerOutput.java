package io.rapid;


public interface LoggerOutput {
	void error(String message, Throwable throwable);
	void info(String message, Throwable throwable);
	void warning(String message, Throwable throwable);
}

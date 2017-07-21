package io.rapid;


import java.io.IOException;


public interface DiskCache {
	void delete() throws IOException;
	void setMaxSize(int maxSizeInBytes);
	void remove(String key) throws IOException;
	String get(String key) throws IOException;
	void put(String key, String value) throws IOException;
}

package io.rapid;


public interface RapidCallback<T> {
	void onValueChanged(T value, ValueMetadata metadata);
}

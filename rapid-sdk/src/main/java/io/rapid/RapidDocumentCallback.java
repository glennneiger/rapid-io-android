package io.rapid;


public interface RapidDocumentCallback<T> {
	void onValueChanged(RapidWrapper<T> value);
}

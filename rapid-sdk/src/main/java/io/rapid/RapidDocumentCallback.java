package io.rapid;


public interface RapidDocumentCallback<T> {
	void onValueChanged(RapidDocument<T> value);
}

package io.rapid;


import java.util.List;


public interface RapidCollectionCallback<T> {
	void onValueChanged(List<RapidDocument<T>> documents);
}

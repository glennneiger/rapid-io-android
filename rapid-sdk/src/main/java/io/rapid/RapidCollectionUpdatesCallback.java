package io.rapid;


import java.util.List;


public interface RapidCollectionUpdatesCallback<T> {
	void onValueChanged(List<RapidDocument<T>> documents, ListUpdate listUpdate);
}

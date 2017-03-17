package io.rapid;


import java.util.Collection;


public interface RapidCollectionCallback<T> {
	void onValueChanged(Collection<RapidWrapper<T>> items);
}

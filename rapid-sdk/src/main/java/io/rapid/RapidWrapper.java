package io.rapid;


public class RapidWrapper<T> {
	private String mId;
	private T mValue;


	RapidWrapper(String id, T value) {
		mId = id;
		mValue = value;
	}


	@Override
	public String toString() {
		return "RapidWrapper(" + getId() + ": " + getValue().toString() + ")";
	}


	public String getId() {
		return mId;
	}


	public T getValue() {
		return mValue;
	}
}

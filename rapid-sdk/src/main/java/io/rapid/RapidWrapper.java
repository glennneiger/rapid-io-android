package io.rapid;


public class RapidWrapper<T> {
	private String id;
	private T body;


	RapidWrapper(String id, T value) {
		this.id = id;
		body = value;
	}


	@Override
	public String toString() {
		return "RapidWrapper(" + getId() + ": " + getBody().toString() + ")";
	}


	public String getId() {
		return id;
	}


	public T getBody() {
		return body;
	}
}

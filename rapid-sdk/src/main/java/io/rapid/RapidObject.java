package io.rapid;


abstract class RapidObject<T> {
	public RapidSubscription<T> subscribe(RapidObjectCallback<T> callback) {
		return new RapidSubscription<T>();
	}
}

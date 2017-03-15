package io.rapid;


abstract class RapidObject<T> {
	public RapidSubscription subscribe(RapidObjectCallback<T> callback) {
		return new RapidSubscription();
	}
}

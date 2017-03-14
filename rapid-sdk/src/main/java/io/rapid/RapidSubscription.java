package io.rapid;


public class RapidSubscription<T> extends RapidFuture<T> {

	private boolean mSubscribed = true;


	public RapidSubscription<T> onError(io.rapid.ErrorCallback callback) {
		return this;
	}


	public void unsubscribe() {
		mSubscribed = false;
	}


	public boolean isSubscribed() {
		return mSubscribed;
	}
}

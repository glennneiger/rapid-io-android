package io.rapid;


public class RapidSubscription {

	private boolean mSubscribed = true;


	public RapidSubscription onError(io.rapid.ErrorCallback callback) {
		return this;
	}


	public void unsubscribe() {
		mSubscribed = false;
	}


	public boolean isSubscribed() {
		return mSubscribed;
	}
}

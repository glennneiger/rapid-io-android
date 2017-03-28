package io.rapid;


class Subscription<T> {
	protected OnUnsubscribeCallback mOnUnsubscribeCallback;
	private boolean mSubscribed = true;


	interface OnUnsubscribeCallback {
		void onUnsubscribe();
	}


	public void unsubscribe() {
		mSubscribed = false;
		if(mOnUnsubscribeCallback != null)
			mOnUnsubscribeCallback.onUnsubscribe();
	}


	public boolean isSubscribed() {
		return mSubscribed;
	}


	public Subscription onError(ErrorCallback callback) {
		return this;
	}
}

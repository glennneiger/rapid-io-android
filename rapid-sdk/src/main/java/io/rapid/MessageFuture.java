package io.rapid;


class MessageFuture {
	private SuccessCallback mSuccessCallback;
	private boolean mSuccess;


	interface SuccessCallback {
		void onSuccess();
	}


	public boolean isCompleted() {
		return true;
	}


	public boolean isSuccessful() {
		return true;
	}


	public MessageFuture onSuccess(SuccessCallback successCallback) {
		mSuccessCallback = successCallback;
		return this;
	}


	public MessageFuture onError(ErrorCallback error) {
		return this;
	}


	void invokeSuccess() {
		mSuccess = true;
		if(mSuccessCallback != null)
			mSuccessCallback.onSuccess();
	}
}

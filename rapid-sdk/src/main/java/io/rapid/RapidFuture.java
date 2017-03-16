package io.rapid;


public class RapidFuture<T> {
	private SuccessCallback mSuccessCallback;
	private boolean mSuccess;


	public interface SuccessCallback {
		void onSuccess();
	}


	public boolean isCompleted() {
		return true;
	}


	public boolean isSuccessful() {
		return true;
	}


	public RapidFuture<T> onSuccess(SuccessCallback successCallback) {
		mSuccessCallback = successCallback;
		return this;
	}


	public RapidFuture<T> onError(ErrorCallback error) {
		return this;
	}


	void invokeSuccess() {
		mSuccess = true;
		if(mSuccessCallback != null)
			mSuccessCallback.onSuccess();
	}
}

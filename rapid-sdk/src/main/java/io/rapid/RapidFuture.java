package io.rapid;


public class RapidFuture {
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


	public RapidFuture onSuccess(SuccessCallback successCallback) {
		mSuccessCallback = successCallback;
		return this;
	}


	public RapidFuture onError(ErrorCallback error) {
		return this;
	}


	void invokeSuccess() {
		mSuccess = true;
		if(mSuccessCallback != null)
			mSuccessCallback.onSuccess();
	}
}

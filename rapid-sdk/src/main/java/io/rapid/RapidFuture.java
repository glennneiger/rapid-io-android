package io.rapid;


public class RapidFuture {
	private SuccessCallback mSuccessCallback;
	private ErrorCallback mErrorCallback;
	private CompleteCallback mCompletedCallback;
	private boolean mSuccess;
	private boolean mCompleted;


	public interface SuccessCallback {
		void onSuccess();
	}


	public interface CompleteCallback {
		void onComplete();
	}


	public boolean isCompleted() {
		return mCompleted;
	}


	public boolean isSuccessful() {
		return mSuccess;
	}


	public RapidFuture onSuccess(SuccessCallback successCallback) {
		mSuccessCallback = successCallback;
		return this;
	}


	public RapidFuture onError(ErrorCallback errorCallback) {
		mErrorCallback = errorCallback;
		mCompleted = true;
		return this;
	}


	public RapidFuture onCompleted(CompleteCallback callback) {
		mCompletedCallback = callback;
		return this;
	}


	void invokeError(RapidError error) {
		mSuccess = false;
		mCompleted = true;
		if(mErrorCallback != null)
			mErrorCallback.onError(error);
		if(mCompletedCallback != null)
			mCompletedCallback.onComplete();
	}


	void invokeSuccess() {
		mSuccess = true;
		mCompleted = true;
		if(mSuccessCallback != null)
			mSuccessCallback.onSuccess();
		if(mCompletedCallback != null)
			mCompletedCallback.onComplete();
	}
}

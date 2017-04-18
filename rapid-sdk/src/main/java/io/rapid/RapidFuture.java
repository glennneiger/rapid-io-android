package io.rapid;


import android.os.Handler;


public class RapidFuture {
	private SuccessCallback mSuccessCallback;
	private RapidCallback.Error mErrorCallback;
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


	public RapidFuture onError(RapidCallback.Error errorCallback) {
		mErrorCallback = errorCallback;
		return this;
	}


	public RapidFuture onCompleted(CompleteCallback callback) {
		mCompletedCallback = callback;
		return this;
	}


	void invokeError(Handler handler, RapidError error) {
		mSuccess = false;
		mCompleted = true;
		if(mErrorCallback != null)
			handler.post(() -> mErrorCallback.onError(error));
		if(mCompletedCallback != null)
			handler.post(() -> mCompletedCallback.onComplete());
	}


	void invokeSuccess(Handler handler) {
		mSuccess = true;
		mCompleted = true;
		if(mSuccessCallback != null)
			handler.post(() -> mSuccessCallback.onSuccess());
		if(mCompletedCallback != null)
			handler.post(() -> mCompletedCallback.onComplete());
	}
}

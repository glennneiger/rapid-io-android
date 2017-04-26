package io.rapid;


import android.os.Handler;


public class RapidFuture {
	private SuccessCallback mSuccessCallback;
	private RapidCallback.Error mErrorCallback;
	private CompleteCallback mCompletedCallback;
	private boolean mSuccess;
	private boolean mCompleted;
	private RapidError mError;
	private Handler mHandler;


	public interface SuccessCallback {
		void onSuccess();
	}


	public interface CompleteCallback {
		void onComplete();
	}


	public RapidFuture(Handler handler) {
		mHandler = handler;
	}


	public boolean isCompleted() {
		return mCompleted;
	}


	public boolean isSuccessful() {
		return mSuccess;
	}


	public RapidFuture onSuccess(SuccessCallback successCallback) {
		if(mSuccess)
			mHandler.post(() -> mSuccessCallback.onSuccess());
		mSuccessCallback = successCallback;
		return this;
	}


	public RapidFuture onError(RapidCallback.Error errorCallback) {
		if(mError != null)
			mHandler.post(() -> mErrorCallback.onError(mError));
		mErrorCallback = errorCallback;
		return this;
	}


	public RapidFuture onCompleted(CompleteCallback callback) {
		if(mCompleted)
			mHandler.post(() -> mCompletedCallback.onComplete());
		mCompletedCallback = callback;
		return this;
	}


	void invokeError(RapidError error) {
		mSuccess = false;
		mCompleted = true;
		mError = error;
		if(mErrorCallback != null)
			mHandler.post(() -> mErrorCallback.onError(error));
		if(mCompletedCallback != null)
			mHandler.post(() -> mCompletedCallback.onComplete());
	}


	void invokeSuccess() {
		mSuccess = true;
		mCompleted = true;
		if(mSuccessCallback != null)
			mHandler.post(() -> mSuccessCallback.onSuccess());
		if(mCompletedCallback != null)
			mHandler.post(() -> mCompletedCallback.onComplete());
	}
}

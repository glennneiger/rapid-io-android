package io.rapid;


import android.support.annotation.NonNull;

import io.rapid.executor.RapidExecutor;


public class RapidFuture {
	private SuccessCallback mSuccessCallback;
	private ErrorCallback mErrorCallback;
	private CompleteCallback mCompletedCallback;
	private boolean mSuccess;
	private boolean mCompleted;
	private RapidError mError;
	private RapidExecutor mHandler;


	public interface SuccessCallback {
		void onSuccess();
	}


	public interface ErrorCallback {
		void onError(RapidError error);
	}


	public interface CompleteCallback {
		void onComplete();
	}


	RapidFuture(RapidExecutor handler) {
		mHandler = handler;
	}


	public boolean isCompleted() {
		return mCompleted;
	}


	public boolean isSuccessful() {
		return mSuccess;
	}


	@NonNull
	public RapidFuture onSuccess(SuccessCallback successCallback) {
		if(mSuccess)
			mHandler.doOnMain(() -> mSuccessCallback.onSuccess());
		mSuccessCallback = successCallback;
		return this;
	}


	@NonNull
	public RapidFuture onError(ErrorCallback errorCallback) {
		if(mError != null)
			mHandler.doOnMain(() -> mErrorCallback.onError(mError));
		mErrorCallback = errorCallback;
		return this;
	}


	@NonNull
	public RapidFuture onCompleted(CompleteCallback callback) {
		if(mCompleted)
			mHandler.doOnMain(() -> mCompletedCallback.onComplete());
		mCompletedCallback = callback;
		return this;
	}


	void invokeError(RapidError error) {
		mSuccess = false;
		mCompleted = true;
		mError = error;
		if(mErrorCallback != null)
			mHandler.doOnMain(() -> mErrorCallback.onError(error));
		if(mCompletedCallback != null)
			mHandler.doOnMain(() -> mCompletedCallback.onComplete());
	}


	void invokeSuccess() {
		mSuccess = true;
		mCompleted = true;
		if(mSuccessCallback != null)
			mHandler.doOnMain(() -> mSuccessCallback.onSuccess());
		if(mCompletedCallback != null)
			mHandler.doOnMain(() -> mCompletedCallback.onComplete());
	}


	void chainTo(@NonNull RapidFuture rapidFuture) {
		rapidFuture
				.onSuccess(this::invokeSuccess)
				.onError(this::invokeError);
	}
}

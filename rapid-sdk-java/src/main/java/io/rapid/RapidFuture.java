package io.rapid;


public class RapidFuture {
	private SuccessCallback mSuccessCallback;
	private ErrorCallback mErrorCallback;
	private CompleteCallback mCompletedCallback;
	private boolean mSuccess;
	private boolean mCompleted;
	private RapidError mError;
	private RapidExecutor mExecutor;


	public interface SuccessCallback {
		void onSuccess();
	}


	public interface ErrorCallback {
		void onError(RapidError error);
	}


	public interface CompleteCallback {
		void onComplete();
	}


	RapidFuture(RapidExecutor executor) {
		mExecutor = executor;
	}


	public boolean isCompleted() {
		return mCompleted;
	}


	public boolean isSuccessful() {
		return mSuccess;
	}


	public RapidFuture onSuccess(SuccessCallback successCallback) {
		if(mSuccess)
			mExecutor.doOnMain(() -> mSuccessCallback.onSuccess());
		mSuccessCallback = successCallback;
		return this;
	}


	public RapidFuture onError(ErrorCallback errorCallback) {
		if(mError != null)
			mExecutor.doOnMain(() -> mErrorCallback.onError(mError));
		mErrorCallback = errorCallback;
		return this;
	}


	public RapidFuture onCompleted(CompleteCallback callback) {
		if(mCompleted)
			mExecutor.doOnMain(() -> mCompletedCallback.onComplete());
		mCompletedCallback = callback;
		return this;
	}


	void invokeError(RapidError error) {
		mSuccess = false;
		mCompleted = true;
		mError = error;
		if(mErrorCallback != null)
			mExecutor.doOnMain(() -> mErrorCallback.onError(error));
		if(mCompletedCallback != null)
			mExecutor.doOnMain(() -> mCompletedCallback.onComplete());
	}


	void invokeSuccess() {
		mSuccess = true;
		mCompleted = true;
		if(mSuccessCallback != null)
			mExecutor.doOnMain(() -> mSuccessCallback.onSuccess());
		if(mCompletedCallback != null)
			mExecutor.doOnMain(() -> mCompletedCallback.onComplete());
	}


	void chainTo(RapidFuture rapidFuture) {
		rapidFuture
				.onSuccess(this::invokeSuccess)
				.onError(this::invokeError);
	}
}

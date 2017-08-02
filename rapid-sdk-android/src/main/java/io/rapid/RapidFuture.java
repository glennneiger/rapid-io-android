package io.rapid;


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


	public RapidFuture onSuccess(SuccessCallback successCallback) {
		if(mSuccess)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mSuccessCallback.onSuccess();}
			});
		mSuccessCallback = successCallback;
		return this;
	}


	public RapidFuture onError(ErrorCallback errorCallback) {
		if(mError != null)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mErrorCallback.onError(mError);}
			});
		mErrorCallback = errorCallback;
		return this;
	}


	public RapidFuture onCompleted(CompleteCallback callback) {
		if(mCompleted)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mCompletedCallback.onComplete();}
			});
		mCompletedCallback = callback;
		return this;
	}


	void invokeError(final RapidError error) {
		mSuccess = false;
		mCompleted = true;
		mError = error;
		if(mErrorCallback != null)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mErrorCallback.onError(error);}
			});
		if(mCompletedCallback != null)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mCompletedCallback.onComplete();}
			});
	}


	void invokeSuccess() {
		mSuccess = true;
		mCompleted = true;
		if(mSuccessCallback != null)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mSuccessCallback.onSuccess();}
			});
		if(mCompletedCallback != null)
			mHandler.doOnMain(new Runnable() {
				@Override
				public void run() {mCompletedCallback.onComplete();}
			});
	}


	void chainTo(RapidFuture rapidFuture) {
		rapidFuture
				.onSuccess(new SuccessCallback() {
					@Override
					public void onSuccess() {RapidFuture.this.invokeSuccess();}
				})
				.onError(new ErrorCallback() {
					@Override
					public void onError(RapidError error) {RapidFuture.this.invokeError(error);}
				});
	}
}

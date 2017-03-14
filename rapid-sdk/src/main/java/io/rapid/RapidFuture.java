package io.rapid;


public class RapidFuture<T> {
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
		return this;
	}


	public RapidFuture<T> onError(ErrorCallback error) {
		return this;
	}
}

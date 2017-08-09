package io.rapid.executor;

import android.support.annotation.NonNull;


public interface RapidExecutor {
	interface Fetchable<T> {
		@NonNull
		T fetch();
	}


	interface FetchableCallback<T> {
		void onFetched(T result);
	}
	void doInBackground(Runnable runnable);
	void doOnMain(Runnable runnable);
	void schedule(Runnable runnable, long delayMs);
	void unschedule(Runnable runnable);
	<T> void fetchInBackground(Fetchable<T> fetchable, FetchableCallback<T> callback);
}
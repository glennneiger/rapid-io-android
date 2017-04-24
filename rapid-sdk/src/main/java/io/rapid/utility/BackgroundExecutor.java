package io.rapid.utility;


import android.os.AsyncTask;


public class BackgroundExecutor {
	public interface Fetchable<T> {
		T fetch();
	}


	public interface FetchableCallback<T> {
		void onFetched(T result);
	}


	public static void doInBackground(Runnable runnable) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				runnable.run();
				return null;
			}
		}.execute();
	}


	public static <T> void fetchInBackground(Fetchable<T> fetchable, FetchableCallback<T> callback) {
		new AsyncTask<Void, Void, T>() {
			@Override
			protected T doInBackground(Void... params) {
				return fetchable.fetch();
			}


			@Override
			protected void onPostExecute(T result) {
				callback.onFetched(result);
			}
		}.execute();
	}
}

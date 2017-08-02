package io.rapid.executor;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;


public class AndroidRapidExecutor implements RapidExecutor {

	private Handler mHandler;
	private Executor mUiExecutor = new Executor() {
		@Override
		public void execute(@NonNull Runnable command) {
			mHandler.post(command);
		}
	};

	private Executor mBackgroundExecutor = AsyncTask.SERIAL_EXECUTOR;


	public AndroidRapidExecutor(Handler handler) {
		mHandler = handler;
	}


	@Override
	public void doInBackground(Runnable runnable) {
		mBackgroundExecutor.execute(runnable);
	}


	@Override
	public void doOnMain(Runnable runnable) {
		mUiExecutor.execute(runnable);
	}


	@Override
	public void schedule(Runnable runnable, long delayMs) {
		mHandler.postDelayed(runnable, delayMs);
	}


	@Override
	public void unschedule(Runnable runnable) {
		mHandler.removeCallbacks(runnable);
	}


	@Override
	public <T> void fetchInBackground(final Fetchable<T> fetchable, final FetchableCallback<T> callback) {
		doInBackground(new Runnable() {
			@Override
			public void run() {
				final T result = fetchable.fetch();
				AndroidRapidExecutor.this.doOnMain(new Runnable() {
					@Override
					public void run() {callback.onFetched(result);}
				});
			}
		});
	}
}
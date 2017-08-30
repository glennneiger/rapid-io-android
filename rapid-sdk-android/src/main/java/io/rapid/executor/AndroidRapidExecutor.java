package io.rapid.executor;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;


public class AndroidRapidExecutor implements RapidExecutor {

	private Handler mHandler;
	@NonNull private Executor mUiExecutor = new Executor() {
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
	public void doInBackground(@NonNull Runnable runnable) {
		mBackgroundExecutor.execute(runnable);
	}


	@Override
	public void doOnMain(@NonNull Runnable runnable) {
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
	public <T> void fetchInBackground(@NonNull Fetchable<T> fetchable, @NonNull FetchableCallback<T> callback) {
		doInBackground(() -> {
			T result = fetchable.fetch();
			doOnMain(() -> callback.onFetched(result));
		});
	}
}
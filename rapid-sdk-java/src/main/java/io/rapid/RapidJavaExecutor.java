package io.rapid;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class RapidJavaExecutor implements RapidExecutor {

    private Executor mBackgroundExecutor = Executors.newSingleThreadExecutor();
    private Executor mMainExecutor = Runnable::run;
    private ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();

    private Map<Runnable, ScheduledFuture> mScheduledRunnables = new HashMap<>();


    @Override
    public void doInBackground(Runnable runnable) {
        mBackgroundExecutor.execute(runnable);
    }

    @Override
    public void doOnMain(Runnable runnable) {
        mMainExecutor.execute(runnable);
    }

    @Override
    public void schedule(Runnable runnable, long delayMs) {
        mScheduledRunnables.put(runnable, mScheduler.schedule(runnable, delayMs, TimeUnit.MILLISECONDS));
    }

    @Override
    public void unschedule(Runnable runnable) {
        if (mScheduledRunnables.containsKey(runnable)) {
            mScheduledRunnables.get(runnable).cancel(true);
        }
    }

    @Override
    public <T> void fetchInBackground(Fetchable<T> fetchable, FetchableCallback<T> callback) {
        doInBackground(() -> {
            T result = fetchable.fetch();
            doOnMain(() -> callback.onFetched(result));
        });
    }
}

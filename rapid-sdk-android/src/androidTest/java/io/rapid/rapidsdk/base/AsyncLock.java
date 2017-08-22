package io.rapid.rapidsdk.base;


import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;


public class AsyncLock {

	@NonNull private CountDownLatch mSignal = new CountDownLatch(1);


	public void lock() {
		try {
			mSignal.await();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}


	public void unlock() {
		mSignal.countDown();
	}
}

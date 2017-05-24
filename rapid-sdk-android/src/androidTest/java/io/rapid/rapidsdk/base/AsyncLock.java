package io.rapid.rapidsdk.base;


import java.util.concurrent.CountDownLatch;


public class AsyncLock {

	private CountDownLatch mSignal = new CountDownLatch(1);


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

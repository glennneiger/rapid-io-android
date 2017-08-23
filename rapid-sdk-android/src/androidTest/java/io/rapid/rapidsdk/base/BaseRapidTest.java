package io.rapid.rapidsdk.base;


import android.support.test.InstrumentationRegistry;

import io.rapid.Rapid;


public class BaseRapidTest {

	private AsyncLock mAsyncLock;


	protected void prepareRapid() {
		prepareRapid(true);
	}


	protected void prepareRapid(boolean authorize) {
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
			Rapid.initialize("NDA1OWE0MWo2N3RicTdyLmFwcC1yYXBpZC5pbw==");
			if(authorize)
				Rapid.getInstance().authorize("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJydWxlcyI6W3siY29sbGVjdGlvbiI6eyJwYXR0ZXJuIjoiLioifSwicmVhZCI6dHJ1ZSwiY3JlYXRlIjp0cnVlLCJ1cGRhdGUiOnRydWUsImRlbGV0ZSI6dHJ1ZX0seyJjaGFubmVsIjp7InBhdHRlcm4iOiIuKiJ9LCJyZWFkIjp0cnVlLCJ3cml0ZSI6dHJ1ZX1dfQ.dqCaHSlyMxtvPas6zFFmLjy8oEgQjgcj0OxEhmul2C0");
		});
	}


	protected void lockAsync() {
		mAsyncLock = new AsyncLock();
		mAsyncLock.lock();
	}


	protected void unlockAsync() {
		mAsyncLock.unlock();
	}
}

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
			Rapid.initialize("ZGV2LXdzLXNlcnZpY2UucmFwaWQuaW8=");
			if(authorize)
				Rapid.getInstance().authorize("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJydWxlcyI6W3siY2hhbm5lbCI6eyJwYXR0ZXJuIjoiLioifSwicmVhZCI6dHJ1ZSwid3JpdGUiOnRydWV9LHsiY29sbGVjdGlvbiI6eyJwYXR0ZXJuIjoiLioifSwicmVhZCI6dHJ1ZSwiY3JlYXRlIjp0cnVlLCJ1cGRhdGUiOnRydWUsImRlbGV0ZSI6dHJ1ZX1dfQ.MdQbdW958yzRQk46qj7_bY92A60pxtSkDgy9yJV7Vd8");
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

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
				Rapid.getInstance().authorize("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbklkIjoiMGU0NzAyOWUtMDY2OC00NGRhLWFkMDItZWQ4N2E5MmQzY2E4IiwicnVsZXMiOlt7ImNvbGxlY3Rpb24iOiIuKiIsInJlYWQiOnRydWUsImNyZWF0ZSI6dHJ1ZSwidXBkYXRlIjp0cnVlLCJkZWxldGUiOnRydWV9XX0.3i4K3AGJp5xYEcpKsliFT8QNvG0pmAi3RHs-0J13rP0");
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

package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.rapid.Rapid;
import io.rapid.RapidCallback;
import io.rapid.RapidError;
import io.rapid.RapidFuture;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static junit.framework.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class TimeOffsetTest extends BaseRapidTest {


	@Before
	public void init() {
		prepareRapid();
	}


	@Test
	public void testOffset() throws InterruptedException {
		Rapid.getInstance().getServerTimeOffset(new RapidCallback.TimeOffset() {
			@Override
			public void onTimeOffsetReceived(long timeOffsetMs) {
				Log.d("OFFSET", timeOffsetMs + "ms");
				TimeOffsetTest.this.unlockAsync();
			}
		}).onError(new RapidFuture.ErrorCallback() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();
	}


}

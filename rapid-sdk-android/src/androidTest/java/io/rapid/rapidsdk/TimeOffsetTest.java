package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.rapid.Rapid;
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
		Rapid.getInstance().getServerTimeOffset(timeOffsetMs -> {
			Log.d("OFFSET", timeOffsetMs + "ms");
			unlockAsync();
		}).onError(error -> fail(error.getMessage()));
		lockAsync();
	}


}

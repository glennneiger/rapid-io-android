package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import io.rapid.Rapid;
import io.rapid.RapidChannelReference;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class ChannelTest extends BaseRapidTest {

	private RapidChannelReference<Car> mChannel;
	private Random mRandom = new Random();


	@Before
	public void init() {
		prepareRapid();
		mChannel = Rapid.getInstance().channel("android_instr_test_channel_001", Car.class);
	}


	@Test
	public void test1() {
		mChannel.subscribe(message -> {
			Log.d("TEST", message.toString());
			assertEquals("test_channel", message.getName());
			assertEquals(7, message.getNumber());
		}).onError(error -> fail(error.getMessage()));

		mChannel.publish(new Car("test_channel", 7))
				.onSuccess(() -> unlockAsync())
				.onError(error -> fail(error.getMessage()));

		lockAsync();
	}


}

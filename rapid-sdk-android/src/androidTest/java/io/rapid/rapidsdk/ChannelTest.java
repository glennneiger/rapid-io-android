package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.rapid.Rapid;
import io.rapid.RapidCallback;
import io.rapid.RapidChannelPrefixReference;
import io.rapid.RapidChannelReference;
import io.rapid.RapidChannelSubscription;
import io.rapid.RapidError;
import io.rapid.RapidFuture;
import io.rapid.RapidMessage;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class ChannelTest extends BaseRapidTest {


	@Before
	public void init() {
		prepareRapid();
	}


	@Test
	public void testNamedChannel() {
		final RapidChannelReference<Car> channel = Rapid.getInstance().channel("android_instr_test_channel_001", Car.class);
		RapidChannelSubscription<Car> subscription = channel.subscribe(new RapidCallback.Message<Car>() {
			@Override
			public void onMessageReceived(RapidMessage<Car> message) {
				assertEquals("test_channel", message.getBody().getName());
				assertEquals(7, message.getBody().getNumber());
				assertEquals(channel.getChannelName(), message.getChannelName());
				ChannelTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});

		channel.publish(new Car("test_channel", 7))
				.onError(new RapidFuture.ErrorCallback() {
					@Override
					public void onError(RapidError error) {fail(error.getMessage());}
				});

		lockAsync();

		subscription.unsubscribe();
	}


	@Test
	public void testPrefix() {
		RapidChannelPrefixReference<Car> channel = Rapid.getInstance().channels("android_instr_test_channel_002_", Car.class);
		RapidChannelSubscription<Car> subscription = channel.subscribe(new RapidCallback.Message<Car>() {
			@Override
			public void onMessageReceived(RapidMessage<Car> message) {
				assertEquals("test_channel", message.getBody().getName());
				assertEquals(7, message.getBody().getNumber());
				assertEquals("android_instr_test_channel_002_xxx", message.getChannelName());
				ChannelTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});

		Rapid.getInstance().channel("android_instr_test_channel_002_xxx", Car.class).publish(new Car("test_channel", 7))
				.onError(new RapidFuture.ErrorCallback() {
					@Override
					public void onError(RapidError error) {fail(error.getMessage());}
				});

		lockAsync();

		subscription.unsubscribe();
	}

}

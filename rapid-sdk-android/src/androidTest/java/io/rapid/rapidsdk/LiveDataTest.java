package io.rapid.rapidsdk;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocument;
import io.rapid.RapidError;
import io.rapid.lifecycle.RapidLiveData;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class LiveDataTest extends BaseRapidTest {

	LifecycleOwner mLifecycleOwner = () -> new Lifecycle() {
		@Override
		public void addObserver(LifecycleObserver observer) {
		}


		@Override
		public void removeObserver(LifecycleObserver observer) {

		}


		@Override
		public State getCurrentState() {
			return State.RESUMED;
		}
	};


	@Test
	public void testSubscribe() {
		prepareRapid();
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_003_" + UUID.randomUUID().toString(), Car.class);

		LiveData<List<RapidDocument<Car>>> liveData = RapidLiveData.from(collection, error -> {
			fail(error.getMessage());
		});

		liveData.observe(mLifecycleOwner, rapidDocuments -> {
			assertNotNull(rapidDocuments);
			unlockAsync();
		});
		lockAsync();

		collection.newDocument().mutate(new Car("asda", 1))
				.onSuccess(() -> unlockAsync())
				.onError(error -> fail(error.getMessage()));
		lockAsync();
	}


	@Test
	public void testError() {
		prepareRapid(false);
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_003_" + UUID.randomUUID().toString(), Car.class);

		LiveData<List<RapidDocument<Car>>> liveData = RapidLiveData.from(collection, error -> {
			assertEquals(error.getType(), RapidError.ErrorType.PERMISSION_DENIED);
			unlockAsync();
		});

		liveData.observe(mLifecycleOwner, rapidDocuments -> {
			fail("Should not get any data");
			unlockAsync();
		});
		lockAsync();
	}

}

package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class CollectionTest extends BaseRapidTest {


	@Before
	public void init() {
	}


	@Test
	public void testLimit() throws InterruptedException {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_002_" + UUID.randomUUID().toString(), Car.class);

		int n = 10;

		// add n documents
		CountDownLatch lock = new CountDownLatch(n);
		for(int i = 0; i < 10; i++) {
			collection.newDocument().mutate(new Car("car", 0))
					.onSuccess(lock::countDown)
					.onError(error -> fail(error.getMessage()));
		}
		lock.await();

		collection.fetch(rapidDocuments -> {
			assertEquals(n, rapidDocuments.size());
			unlockAsync();
		});
		lockAsync();

		int m = 2;
		collection.limit(m).fetch(rapidDocuments -> {
			assertEquals(m+1
					, rapidDocuments.size());
			unlockAsync();
		}).onError(error -> fail(error.getMessage()));
		lockAsync();


	}


}

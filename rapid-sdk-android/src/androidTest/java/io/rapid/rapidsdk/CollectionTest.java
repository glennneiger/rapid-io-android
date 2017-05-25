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
		testLimit(10, 1);
		testLimit(10, 10);
		testLimit(10, 20);
	}


	private void testLimit(int numDocs, int limit) throws InterruptedException {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_002_" + UUID.randomUUID().toString(), Car.class);

		// add n documents
		CountDownLatch lock = new CountDownLatch(numDocs);
		for(int i = 0; i < numDocs; i++) {
			collection.newDocument().mutate(new Car("car", 0))
					.onSuccess(lock::countDown)
					.onError(error -> fail(error.getMessage()));
		}
		lock.await();

		collection.fetch(rapidDocuments -> {
			assertEquals(numDocs, rapidDocuments.size());
			unlockAsync();
		});
		lockAsync();

		collection.limit(limit).fetch(rapidDocuments -> {
			assertEquals(Math.min(limit, numDocs), rapidDocuments.size());
			unlockAsync();
		}).onError(error -> {
			fail(error.getMessage());
		});
		lockAsync();
	}


}

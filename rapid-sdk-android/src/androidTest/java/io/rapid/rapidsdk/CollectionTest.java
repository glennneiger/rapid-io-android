package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocument;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class CollectionTest extends BaseRapidTest {


	@Before
	public void init() {
		prepareRapid();
	}


	@Test
	public void testLimit() throws InterruptedException {
		testLimit(10, 1);
		testLimit(10, 10);
		testLimit(10, 20);
	}


	@Test
	public void testOrder() throws InterruptedException {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_002_" + UUID.randomUUID().toString(), Car.class);

		// add n documents
		CountDownLatch lock = new CountDownLatch(10);
		for(int i = 0; i < 10; i++) {
			collection.newDocument().mutate(new Car("car", new Random().nextInt()))
					.onSuccess(lock::countDown)
					.onError(error -> fail(error.getMessage()));
		}
		lock.await();

		collection.orderBy("number").fetch(rapidDocuments -> {
			ArrayList<RapidDocument<Car>> orderedList = new ArrayList<>(rapidDocuments);
			Collections.sort(orderedList, (o1, o2) -> Long.compare(o1.getBody().getNumber(), o2.getBody().getNumber()));
			for(int i = 0; i < orderedList.size(); i++) {
				assertEquals("Order does not match", rapidDocuments.get(i).getId(), orderedList.get(i).getId());
			}
			unlockAsync();
		}).onError(error -> {
			fail(error.getMessage());
		});
		lockAsync();
	}


	@Test
	public void testSubscribe() {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_003_" + UUID.randomUUID().toString(), Car.class);
		collection.subscribe(rapidDocuments -> {
			assertNotNull(rapidDocuments);
			unlockAsync();
		}).onError(error -> fail(error.getMessage()));
		lockAsync();

		collection.newDocument().mutate(new Car("asda", 1))
				.onSuccess(() -> unlockAsync())
				.onError(error -> fail(error.getMessage()));
		lockAsync();
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

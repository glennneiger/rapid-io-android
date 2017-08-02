package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.rapid.Rapid;
import io.rapid.RapidCallback;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocument;
import io.rapid.RapidError;
import io.rapid.RapidFuture;
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
		final CountDownLatch lock = new CountDownLatch(10);
		for(int i = 0; i < 10; i++) {
			collection.newDocument().mutate(new Car("car", new Random().nextInt()))
					.onSuccess(new RapidFuture.SuccessCallback() {
						@Override
						public void onSuccess() {lock.countDown();}
					})
					.onError(new RapidFuture.ErrorCallback() {
						@Override
						public void onError(RapidError error) {fail(error.getMessage());}
					});
		}
		lock.await();

		collection.orderBy("number").fetch(new RapidCallback.Collection<Car>() {
			@Override
			public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
				ArrayList<RapidDocument<Car>> orderedList = new ArrayList<>(rapidDocuments);
				Collections.sort(orderedList, new Comparator<RapidDocument<Car>>() {
					@Override
					public int compare(RapidDocument<Car> o1, RapidDocument<Car> o2) {return Long.compare(o1.getBody().getNumber(), o2.getBody().getNumber());}
				});
				for(int i = 0; i < orderedList.size(); i++) {
					assertEquals("Order does not match", rapidDocuments.get(i).getId(), orderedList.get(i).getId());
				}
				CollectionTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {
				fail(error.getMessage());
			}
		});
		lockAsync();
	}


	@Test
	public void testSubscribe() {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_003_" + UUID.randomUUID().toString(), Car.class);
		collection.subscribe(new RapidCallback.Collection<Car>() {
			@Override
			public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
				assertNotNull(rapidDocuments);
				CollectionTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();

		collection.newDocument().mutate(new Car("asda", 1))
				.onSuccess(new RapidFuture.SuccessCallback() {
					@Override
					public void onSuccess() {CollectionTest.this.unlockAsync();}
				})
				.onError(new RapidFuture.ErrorCallback() {
					@Override
					public void onError(RapidError error) {fail(error.getMessage());}
				});
		lockAsync();
	}


	private void testLimit(final int numDocs, final int limit) throws InterruptedException {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_002_" + UUID.randomUUID().toString(), Car.class);

		// add n documents
		final CountDownLatch lock = new CountDownLatch(numDocs);
		for(int i = 0; i < numDocs; i++) {
			collection.newDocument().mutate(new Car("car", 0))
					.onSuccess(new RapidFuture.SuccessCallback() {
						@Override
						public void onSuccess() {lock.countDown();}
					})
					.onError(new RapidFuture.ErrorCallback() {
						@Override
						public void onError(RapidError error) {fail(error.getMessage());}
					});
		}
		lock.await();

		collection.fetch(new RapidCallback.Collection<Car>() {
			@Override
			public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
				assertEquals(numDocs, rapidDocuments.size());
				CollectionTest.this.unlockAsync();
			}
		});
		lockAsync();

		collection.limit(limit).fetch(new RapidCallback.Collection<Car>() {
			@Override
			public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
				assertEquals(Math.min(limit, numDocs), rapidDocuments.size());
				CollectionTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {
				fail(error.getMessage());
			}
		});
		lockAsync();
	}

	@Test
	public void testUnsubscribe() throws InterruptedException {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_002_" + UUID.randomUUID().toString(), Car.class);
		RapidCollectionSubscription sub = collection.subscribe(new RapidCallback.Collection<Car>() {
			@Override
			public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
				CollectionTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});

		lockAsync();
		sub.unsubscribe();
		Thread.sleep(5000);
	}

	@Test
	public void testNull() {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_003_" + UUID.randomUUID().toString(), Car.class);
		collection
				.equalTo("name", (String) null)
				.isNull("name")
				.isNotNull("number")
				.subscribe(new RapidCallback.Collection<Car>() {
					@Override
					public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
						assertNotNull(rapidDocuments);
						CollectionTest.this.unlockAsync();
					}
				}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();

	}

}

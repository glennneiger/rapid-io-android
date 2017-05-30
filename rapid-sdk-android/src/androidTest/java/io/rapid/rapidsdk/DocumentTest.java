package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.rapid.Etag;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocumentExecutor;
import io.rapid.RapidDocumentReference;
import io.rapid.RapidError;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class DocumentTest extends BaseRapidTest {

	private RapidCollectionReference<Car> mCollection;
	private Random mRandom = new Random();


	@Before
	public void init() {
		prepareRapid();
		mCollection = Rapid.getInstance().collection("android_instr_test_001", Car.class);
	}


	@Test
	public void testDocumentAddAndFetch() {
		RapidDocumentReference<Car> newDoc = mCollection.newDocument();
		int carNumber = mRandom.nextInt();
		newDoc.mutate(new Car("car_1", carNumber)).onSuccess(() -> {
			newDoc.fetch(value -> {
				assertEquals(newDoc.getId(), value.getId());
				assertEquals(carNumber, value.getBody().getNumber());
				unlockAsync();
			});
		});
		lockAsync();
	}


	@Test
	public void testConcurrencySafety() throws InterruptedException {
		int n = 10;
		String id = UUID.randomUUID().toString();
		CountDownLatch lock = new CountDownLatch(n);
		for(int i = 0; i < n; i++) {
			mCollection.document(id).execute(oldDocument -> {
				Car car;
				if(oldDocument == null)
					car = new Car("car_x", 1);
				else {
					car = oldDocument.getBody();
					car.setNumber(car.getNumber() + 1);
				}
				return RapidDocumentExecutor.mutate(car);
			}).onCompleted(lock::countDown);
		}
		lock.await();

		mCollection.document(id).fetch(document -> {
			assertEquals(n, document.getBody().getNumber());
			unlockAsync();
		});
		lockAsync();
	}


	@Test
	public void testAddRemove() {
		String id = UUID.randomUUID().toString();
		mCollection.document(id).fetch(document -> {
			assertNull(document);
			mCollection.document(id).mutate(new Car("xxx", 1)).onSuccess(() -> {
				mCollection.document(id).fetch(document2 -> {
					assertNotNull(document2);
					mCollection.document(id).delete().onSuccess(() -> {
						mCollection.document(id).fetch(document3 -> {
							assertNull(document3);
							unlockAsync();
						});
					});
				});
			});
		});
		lockAsync();
	}


	@Test
	public void testSimpleMutate() {
		String id = UUID.randomUUID().toString();
		mCollection.document(id).mutate(new Car("ford", 7)).onSuccess(() -> {
			mCollection.document(id).fetch(document -> {
				System.out.print("--------");
				assertEquals(id, document.getId());
				assertEquals("ford", document.getBody().getName());
				assertEquals(7, document.getBody().getNumber());
				unlockAsync();
			});
		});
		lockAsync();
	}


	@Test
	public void testMutateWithEtag() {
		String id = UUID.randomUUID().toString();

		mCollection.document(id).mutate(new Car("car", 0), Etag.fromValue("random"))
				.onSuccess(Assert::fail)
				.onError(error -> {
					assertEquals(RapidError.ErrorType.ETAG_CONFLICT, error.getType());
					unlockAsync();
				});
		lockAsync();

		mCollection.document(id).mutate(new Car("car", 0), Etag.NO_ETAG)
				.onError(error -> fail())
				.onSuccess(this::unlockAsync);
		lockAsync();
	}


}

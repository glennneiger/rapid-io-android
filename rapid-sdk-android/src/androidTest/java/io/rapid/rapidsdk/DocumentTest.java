package io.rapid.rapidsdk;

import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocumentReference;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class DocumentTest extends BaseRapidTest {

	private RapidCollectionReference<Car> mCollection;
	private Random mRandom = new Random();
	private Handler mHandler;


	@Before
	public void init() {
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> mHandler = new Handler());
		mCollection = Rapid.getInstance().collection("android_instr_test_001", Car.class);
	}


	@Test
	public void testDocumentAddAndFetch() throws InterruptedException {
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
			mCollection.document(id).concurrencySafeMutate(oldDocument -> {
				Car car;
				if(oldDocument == null)
					car = new Car("car_x", 1);
				else {
					car = oldDocument.getBody();
					car.setNumber(car.getNumber() + 1);
				}
				return car;
			}).onCompleted(lock::countDown);
		}
		lock.await();

		mCollection.document(id).fetch(document -> {
			assertEquals(n, document.getBody().getNumber());
			unlockAsync();
		});
		lockAsync();
	}
}

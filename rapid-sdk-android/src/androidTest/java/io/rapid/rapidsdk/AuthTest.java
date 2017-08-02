package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import io.rapid.Rapid;
import io.rapid.RapidCallback;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocument;
import io.rapid.RapidError;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class AuthTest extends BaseRapidTest {


	@Before
	public void init() {
		prepareRapid(false);
	}


	@Test
	public void testAuth() throws InterruptedException {
		RapidCollectionReference<Car> collection = Rapid.getInstance().collection("android_instr_test_003_" + UUID.randomUUID().toString(), Car.class);
		collection.fetch(new RapidCallback.Collection<Car>() {
			@Override
			public void onValueChanged(List<RapidDocument<Car>> rapidDocuments) {
				fail();
				AuthTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {
				assertEquals(RapidError.ErrorType.PERMISSION_DENIED, error.getType());
				AuthTest.this.unlockAsync();
			}
		});
		lockAsync();
	}


}

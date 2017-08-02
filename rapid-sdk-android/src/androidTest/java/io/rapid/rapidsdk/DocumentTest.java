package io.rapid.rapidsdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.rapid.Etag;
import io.rapid.Rapid;
import io.rapid.RapidActionFuture;
import io.rapid.RapidCallback;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocument;
import io.rapid.RapidDocumentExecutor;
import io.rapid.RapidDocumentReference;
import io.rapid.RapidError;
import io.rapid.RapidFuture;
import io.rapid.RapidMutateOptions;
import io.rapid.rapidsdk.base.BaseRapidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
		final RapidDocumentReference<Car> newDoc = mCollection.newDocument();
		final int carNumber = mRandom.nextInt();
		newDoc.mutate(new Car("car_1", carNumber)).onSuccess(new RapidFuture.SuccessCallback() {
			@Override
			public void onSuccess() {
				newDoc.fetch(new RapidCallback.Document<Car>() {
					@Override
					public void onValueChanged(RapidDocument<Car> value) {
						assertEquals(newDoc.getId(), value.getId());
						assertEquals(carNumber, value.getBody().getNumber());
						DocumentTest.this.unlockAsync();
					}
				});
			}
		});
		lockAsync();
	}


	@Test
	public void testConcurrencySafety() throws InterruptedException {
		final int n = 10;
		String id = UUID.randomUUID().toString();
		final CountDownLatch lock = new CountDownLatch(n);
		for(int i = 0; i < n; i++) {
			mCollection.document(id).execute(new RapidDocumentExecutor.Callback<Car>() {
				@Override
				public RapidDocumentExecutor.Result execute(RapidDocument<Car> oldDocument) {
					Car car;
					if(oldDocument == null)
						car = new Car("car_x", 1);
					else {
						car = oldDocument.getBody();
						car.setNumber(car.getNumber() + 1);
					}
					return RapidDocumentExecutor.mutate(car);
				}
			}).onCompleted(new RapidFuture.CompleteCallback() {
				@Override
				public void onComplete() {lock.countDown();}
			});
		}
		lock.await();

		mCollection.document(id).fetch(new RapidCallback.Document<Car>() {
			@Override
			public void onValueChanged(RapidDocument<Car> document) {
				assertEquals(n, document.getBody().getNumber());
				DocumentTest.this.unlockAsync();
			}
		});
		lockAsync();
	}


	@Test
	public void testAddRemove() {
		final String id = UUID.randomUUID().toString();
		mCollection.document(id).fetch(new RapidCallback.Document<Car>() {
			@Override
			public void onValueChanged(RapidDocument<Car> document) {
				assertNull(document);
				mCollection.document(id).mutate(new Car("xxx", 1)).onSuccess(new RapidFuture.SuccessCallback() {
					@Override
					public void onSuccess() {
						mCollection.document(id).fetch(new RapidCallback.Document<Car>() {
							@Override
							public void onValueChanged(RapidDocument<Car> document2) {
								assertNotNull(document2);
								mCollection.document(id).delete().onSuccess(new RapidFuture.SuccessCallback() {
									@Override
									public void onSuccess() {
										mCollection.document(id).fetch(new RapidCallback.Document<Car>() {
											@Override
											public void onValueChanged(RapidDocument<Car> document3) {
												assertNull(document3);
												DocumentTest.this.unlockAsync();
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
		lockAsync();
	}


	@Test
	public void testSimpleMutate() {
		final String id = UUID.randomUUID().toString();
		mCollection.document(id).mutate(new Car("ford", 7)).onSuccess(new RapidFuture.SuccessCallback() {
			@Override
			public void onSuccess() {
				mCollection.document(id).fetch(new RapidCallback.Document<Car>() {
					@Override
					public void onValueChanged(RapidDocument<Car> document) {
						System.out.print("--------");
						assertEquals(id, document.getId());
						assertEquals("ford", document.getBody().getName());
						assertEquals(7, document.getBody().getNumber());
						DocumentTest.this.unlockAsync();
					}
				});
			}
		});
		lockAsync();
	}


	@Test
	public void testMutateWithEtag() {
		String id = UUID.randomUUID().toString();

		RapidMutateOptions options = new RapidMutateOptions.Builder()
				.expectEtag(Etag.fromValue("random"))
				.build();

		mCollection.document(id).mutate(new Car("car", 0), options)
				.onSuccess(new RapidFuture.SuccessCallback() {
					@Override
					public void onSuccess() {Assert.fail();}
				})
				.onError(new RapidFuture.ErrorCallback() {
					@Override
					public void onError(RapidError error) {
						assertEquals(RapidError.ErrorType.ETAG_CONFLICT, error.getType());
						DocumentTest.this.unlockAsync();
					}
				});

		lockAsync();

		RapidMutateOptions options2 = new RapidMutateOptions.Builder()
				.expectEtag(Etag.NO_ETAG)
				.build();

		mCollection.document(id).mutate(new Car("car", 0), options2)
				.onError(new RapidFuture.ErrorCallback() {
					@Override
					public void onError(RapidError error) {fail();}
				})
				.onSuccess(new RapidFuture.SuccessCallback() {
					@Override
					public void onSuccess() {DocumentTest.this.unlockAsync();}
				});

		lockAsync();
	}


	@Test
	public void testTimestampServerValue() {
		RapidDocumentReference<Car> doc = mCollection.newDocument();

		RapidMutateOptions options = new RapidMutateOptions.Builder()
				.fillPropertyWithServerTimestamp("number")
				.build();

		doc.mutate(new Car("name", 0), options)
				.onSuccess(new RapidFuture.SuccessCallback() {
					@Override
					public void onSuccess() {DocumentTest.this.unlockAsync();}
				})
				.onError(new RapidFuture.ErrorCallback() {
					@Override
					public void onError(RapidError error) {fail(error.getMessage());}
				});
		lockAsync();

		doc.fetch(new RapidCallback.Document<Car>() {
			@Override
			public void onValueChanged(RapidDocument<Car> document) {
				assertNotEquals(0, document.getBody().getNumber());
				DocumentTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();
	}


	@Test
	public void testNotExistingDoc() {
		mCollection.document(UUID.randomUUID().toString()).fetch(new RapidCallback.Document<Car>() {
			@Override
			public void onValueChanged(RapidDocument<Car> document) {
				assertNull(document);
				DocumentTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();
	}


	@Test
	public void testDocumentAddMergeAndFetch() {
		RapidDocumentReference<Car> newDoc = mCollection.newDocument();
		final int carNumber = mRandom.nextInt();

		newDoc.mutate(new Car("car_1", carNumber)).onSuccess(new RapidFuture.SuccessCallback() {
			@Override
			public void onSuccess() {DocumentTest.this.unlockAsync();}
		}).onError(new RapidFuture.ErrorCallback() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();

		Map<String, Object> mergeMap = new HashMap<>();
		mergeMap.put("number", carNumber + 1);
		newDoc.merge(mergeMap).onSuccess(new RapidFuture.SuccessCallback() {
			@Override
			public void onSuccess() {DocumentTest.this.unlockAsync();}
		}).onError(new RapidFuture.ErrorCallback() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();

		newDoc.fetch(new RapidCallback.Document<Car>() {
			@Override
			public void onValueChanged(RapidDocument<Car> document) {
				assertEquals(carNumber + 1, document.getBody().getNumber());
				assertEquals("car_1", document.getBody().getName());
				DocumentTest.this.unlockAsync();
			}
		}).onError(new RapidCallback.Error() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		lockAsync();
	}

	@Test
	public void testDisconnectAction() {
		RapidDocumentReference<Car> doc = mCollection.newDocument();

		RapidActionFuture f = doc.onDisconnect().mutate(new Car("car-2", 2334));
		f.onError(new RapidFuture.ErrorCallback() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		});
		f.onSuccess(new RapidFuture.SuccessCallback() {
			@Override
			public void onSuccess() {DocumentTest.this.unlockAsync();}
		});
		lockAsync();

		f.cancel().onError(new RapidFuture.ErrorCallback() {
			@Override
			public void onError(RapidError error) {fail(error.getMessage());}
		}).onSuccess(new RapidFuture.SuccessCallback() {
			@Override
			public void onSuccess() {DocumentTest.this.unlockAsync();}
		});
		lockAsync();
	}

}

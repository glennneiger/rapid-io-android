package io.rapid.sample;


import android.util.Log;

import io.rapid.Rapid;
import io.rapid.RapidCollection;
import io.rapid.RapidSubscription;
import io.rapid.Sorting;


public class SampleUsage {

	private static final String RAPID_API_KEY = "sdafh87923jweql2393rfksad";


	public static void sampleMethod() {
		// initialize SDK
		Rapid.initialize(RAPID_API_KEY);


		// set custom JSON converter
		Rapid.getInstance().setJsonConverter(new RapidJacksonConverter());


		// get Rapid collection
		RapidCollection<Car> cars = Rapid.getInstance().collection("cars", Car.class);


		// simple subscription
		RapidSubscription carsSubscription = cars.subscribe((carCollection, metadata) -> log(carCollection.toString()));

		// unsubscribe when not needed anymore
		carsSubscription.unsubscribe();


		// error handling
		cars.subscribe((carCollection, metadata) -> log(carCollection.toString()))
				.onError(error -> log("Subscribe error"));


		// filtering
		cars.query()
				.equalTo("type", "SUV")
				.between("price", 0, 45000)
				.subscribe((carCollection, metadata) -> {
					log(carCollection.toString());
				});


		// advanced filtering
		cars.query()
				.between("price", 0, 45000)
				.beginGroup()
				.equalTo("type", "SUV")
				.or()
				.equalTo("type", "sedan")
				.endGroup()
				.orderBy("price", Sorting.ASC)
				.skip(20)
				.limit(20)
				.subscribe((carCollection, metadata) -> {
					log(carCollection.toString());
				});

		// basic mutation
		cars.add(new Car());


		// advanced mutation
		cars.add(new Car())
				.onSuccess(() -> {
					log("Mutation successful");
				})
				.onError(error -> {
					log("Mutation error");
					error.printStackTrace();
				});
	}


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}
}

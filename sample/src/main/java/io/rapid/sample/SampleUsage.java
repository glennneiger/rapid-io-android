package io.rapid.sample;


import android.util.Log;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocumentReference;
import io.rapid.RapidSubscription;
import io.rapid.Sorting;


public class SampleUsage {

	private static final String RAPID_API_KEY = "sdafh87923jweql2393rfksad";


	public static void sampleMethod() {
		// initialize SDK
		Rapid.initialize(RAPID_API_KEY);


		// mutate custom JSON converter
		Rapid.getInstance().setJsonConverter(new RapidJacksonConverter());


		// get Rapid collection
		RapidCollectionReference<Car> cars = Rapid.getInstance().collection("cars", Car.class);


		// simple subscription
		RapidSubscription carsSubscription = cars.subscribe((carCollection) -> log(carCollection.toString()));

		// unsubscribe when not needed anymore
		carsSubscription.unsubscribe();


		// document subscription
		cars.document("asdfasdfasdf").subscribe(value -> {
			log(value.getBody().toString());
		});


		// error handling
		cars.subscribe((carCollection) -> log(carCollection.toString()))
				.onError(error -> log("Subscribe error"));


		// filtering
		cars.equalTo("type", "SUV")
				.between("price", 0, 45000)
				.subscribe((carCollection) -> {
					log(carCollection.toString());
				});


		// advanced filtering
		cars.between("price", 0, 45000)
				.beginGroup()
				.equalTo("type", "SUV")
				.or()
				.equalTo("type", "sedan")
				.endGroup()
				.orderBy("price", Sorting.ASC)
				.skip(20)
				.limit(20)
				.subscribe((carCollection) -> {
					log(carCollection.toString());
				});


		// basic adding
		RapidDocumentReference<Car> newCar = cars.newDocument();

		log(newCar.getId());

		newCar.mutate(new Car());


		// advanced adding
		cars.newDocument().mutate(new Car())
				.onSuccess(() -> {
					log("Mutation successful");
				})
				.onError(error -> {
					log("Mutation error");
					error.printStackTrace();
				});


		// editing
		cars.document("asfasdfwewqer").mutate(new Car());
	}


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}
}

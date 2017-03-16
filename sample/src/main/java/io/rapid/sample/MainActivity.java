package io.rapid.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Random;

import io.rapid.Rapid;
import io.rapid.RapidSubscription;


public class MainActivity extends AppCompatActivity {

	public static final String COLLECTIONS_CARS = "cars";
	private static final String RAPID_API_KEY = "sdafh87923jweql2393rfksad";
	private RapidSubscription mSubscription;


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Rapid.initialize(RAPID_API_KEY);
		Rapid.getInstance().setJsonConverter(new RapidJacksonConverter());


		mSubscription = Rapid.getInstance().collection(COLLECTIONS_CARS, Car.class)
				.subscribe((carCollection, metadata) -> log(carCollection.toString()));
	}


	@Override
	protected void onDestroy() {
		mSubscription.unsubscribe();
		super.onDestroy();
	}


	public void addItem(View view) {
		Car newCar = new Car(new Random().nextInt());

		Rapid.getInstance()
				.collection(COLLECTIONS_CARS, Car.class)
				.newDocument()
				.mutate(newCar)
				.onSuccess(() -> {
					log("Mutation successful");
				})
				.onError(error -> {
					log("Mutation error");
					error.printStackTrace();
				});
	}
}

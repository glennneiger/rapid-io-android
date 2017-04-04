package io.rapid.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Random;

import io.rapid.Rapid;
import io.rapid.RapidCollectionSubscription;
import io.rapid.Sorting;
import io.rapid.sample.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements CarItemViewModel.CarItemHandler {

	public static final String COLLECTIONS_CARS = "cars_2";
	private static final String RAPID_API_KEY = "sdafh87923jweql2393rfksad";
	private RapidCollectionSubscription mSubscription;
	private ActivityMainBinding mBinding;
	private MainViewModel mViewModel;


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = new MainViewModel();
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		mBinding.setViewModel(mViewModel);

		Rapid.initialize(getApplication(), RAPID_API_KEY);

		Rapid.getInstance().addConnectionStateListener(state -> log(state.toString()));

		mSubscription = Rapid.getInstance().collection(COLLECTIONS_CARS, Car.class)
				.equalTo("receiver", "carl01")
				.beginOr()
				.equalTo("sender", "john123")
				.greaterOrEqualThan("urgency", 1)
				.endOr()
				.orderBy("sentDate", Sorting.DESC)
				.orderBy("urgency", Sorting.ASC)
				.map(document -> new CarItemViewModel(document.getId(), document.getBody(), MainActivity.this))
				.subscribe(items -> mViewModel.items.update(items));
	}


	@Override
	protected void onDestroy() {
		Rapid.getInstance().removeAllConnectionStateListeners();
		mSubscription.unsubscribe();
		super.onDestroy();
	}


	public void addItem(View view) {
		Car newCar = new Car(new Random().nextInt(), "New model");

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


	@Override
	public void onDelete(String carId, Car car) {
		Rapid.getInstance().collection(COLLECTIONS_CARS, Car.class).document(carId).delete().onSuccess(() -> Log.d("CARS", "Deleted"));
	}
}

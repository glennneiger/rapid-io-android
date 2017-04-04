package io.rapid.sample;


public class CarItemViewModel {
	private final String mCarId;
	private Car mCar;
	private CarItemHandler mHandler;


	public interface CarItemHandler {
		void onDelete(String carId, Car car);
	}


	public CarItemViewModel(String carId, Car car, CarItemHandler handler) {
		mCarId = carId;
		mCar = car;
		mHandler = handler;
	}


	public Car getCar() {
		return mCar;
	}


	public void onLongClick() {
		mHandler.onDelete(mCarId, mCar);
	}
}

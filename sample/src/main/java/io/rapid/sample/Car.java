package io.rapid.sample;


public class Car {
	private int mNumber;


	public Car() {
	}


	public Car(int number) {
		mNumber = number;
	}


	@Override
	public String toString() {
		return "Car number " + mNumber;
	}


	public int getNumber() {
		return mNumber;
	}


	public void setNumber(int number) {
		mNumber = number;
	}
}

package io.rapid.sample;


public class Car {
	private int number;


	public Car() {
	}


	public Car(int number) {
		this.number = number;
	}


	@Override
	public String toString() {
		return "Car number " + number;
	}


	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}
}

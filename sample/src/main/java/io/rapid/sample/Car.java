package io.rapid.sample;


import io.rapid.Index;

public class Car {
	@Index
	private int number;
	private String model;


	public Car() {
	}


	public Car(int number, String model) {
		this.number = number;
		this.model = model;
	}


	@Override
	public String toString() {
		return "Car number " + number + ", model: " + model;
	}


	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}


	public String getModel()
	{
		return model;
	}


	public void setModel(String model)
	{
		this.model = model;
	}
}

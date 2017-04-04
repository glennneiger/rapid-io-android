package io.rapid.sample;


import com.google.gson.annotations.SerializedName;

import io.rapid.Index;

public class Car {
	@Index
	@SerializedName("number")
	private int mNumber;
	@Index("model")
	private String model;


	public Car() {
	}


	public Car(int number, String model) {
		this.mNumber = number;
		this.model = model;
	}


	@Override
	public String toString() {
		return "Car number " + mNumber + ", model: " + model;
	}


	public int getNumber() {
		return mNumber;
	}


	public void setNumber(int number) {
		this.mNumber = number;
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

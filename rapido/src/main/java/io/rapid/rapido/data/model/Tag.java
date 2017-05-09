package io.rapid.rapido.data.model;


public class Tag {
	String name;
	int color;


	public Tag(String name, int color) {
		this.name = name;
		this.color = color;
	}


	public String getName() {
		return name;
	}


	public int getColor() {
		return color;
	}
}

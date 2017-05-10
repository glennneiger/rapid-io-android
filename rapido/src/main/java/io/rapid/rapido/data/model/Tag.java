package io.rapid.rapido.data.model;


import android.graphics.Color;

import java.util.Arrays;
import java.util.List;


public class Tag {
	String name;
	int color;


	public Tag(String name, int color) {
		this.name = name;
		this.color = color;
	}


	public static List<Tag> getAllTags() {
		return Arrays.asList(
				new Tag("home", Color.parseColor("#F44336")),
				new Tag("work", Color.parseColor("#795548")),
				new Tag("other", Color.parseColor("#FFC107"))
		);
	}


	public String getName() {
		return name;
	}


	public int getColor() {
		return color;
	}
}

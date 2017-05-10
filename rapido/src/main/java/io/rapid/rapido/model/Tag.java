package io.rapid.rapido.model;


import android.graphics.Color;

import java.util.Arrays;
import java.util.List;


/**
 * POJO representation of Tag
 * <p>
 * A Tag is represented by its name and color
 */
public class Tag {
	private String name;
	private int color;


	private Tag(String name, int color) {
		this.name = name;
		this.color = color;
	}


	/**
	 * Fetch hardcoded list of tags used for tasks
	 */
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

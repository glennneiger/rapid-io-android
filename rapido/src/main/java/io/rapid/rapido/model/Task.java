package io.rapid.rapido.model;


import java.util.Date;
import java.util.Set;


/**
 * Main model class (POJO) representing one Task within this sample app
 * <p>
 * This class is automatically serialized/deserialized to JSON and saved within Rapid.io collection.
 * Serialization/Deserialization is performed by Gson library (@link https://github.com/google/gson)
 */
public class Task {
	private String title;
	private String description;
	private Date createdAt;
	private int priority;
	private Set<String> tags;
	private boolean done;


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public Date getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	public Set<String> getTags() {
		return tags;
	}


	public void setTags(Set<String> tags) {
		this.tags = tags;
	}


	public boolean isDone() {
		return done;
	}


	public void setDone(boolean done) {
		this.done = done;
	}
}

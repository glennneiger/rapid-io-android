package io.rapid.rapido.model;


import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.Date;
import java.util.List;

import io.rapid.rapido.BR;


public class Task extends BaseObservable {
	private String title;
	private String description;
	private Date createdAt;
	private int priority;
	private List<String> tags;
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


	public List<String> getTags() {
		return tags;
	}


	public void setTags(List<String> tags) {
		this.tags = tags;
	}


	@Bindable
	public boolean isDone() {
		return done;
	}


	public void setDone(boolean done) {
		this.done = done;
		notifyPropertyChanged(BR.done);
	}


	@Override
	public String toString() {
		return "Task{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", createdAt=" + createdAt +
				", priority=" + priority +
				", tags=" + tags +
				", done=" + done +
				'}';
	}
}

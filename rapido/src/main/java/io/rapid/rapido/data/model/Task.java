package io.rapid.rapido.data.model;


import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.Date;
import java.util.Set;

import io.rapid.rapido.BR;


public class Task extends BaseObservable {
	private String title;
	private String description;
	private Date createdAt;
	private int priority;
	private Set<String> tags;
	private boolean done;


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


	@Bindable
	public boolean isDone() {
		return done;
	}


	public void setDone(boolean done) {
		this.done = done;
		notifyPropertyChanged(BR.done);
	}
}

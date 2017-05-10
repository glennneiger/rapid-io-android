package io.rapid.rapido.ui.list.item;


import java.util.ArrayList;
import java.util.List;

import io.rapid.RapidDocument;
import io.rapid.rapido.model.Tag;
import io.rapid.rapido.model.Task;


public class TaskItemViewModel {
	private final RapidDocument<Task> mDocument;
	private TaskItemHandler mHandler;
	private List<Tag> mTags;


	public TaskItemViewModel(RapidDocument<Task> document, TaskItemHandler handler) {
		mDocument = document;
		mHandler = handler;
		mTags = new ArrayList<>();
		if(getTask().getTags() != null) {
			for(Tag tag : Tag.getAllTags()) {
				if(getTask().getTags().contains(tag.getName()))
					mTags.add(tag);
			}
		}
	}


	public Task getTask() {
		return mDocument.getBody();
	}


	public void edit() {
		mHandler.editTask(getId(), getTask());
	}


	public void onCheckedChanged(boolean checked) {
		if(getTask().isDone() != checked) {
			getTask().setDone(checked);
			mHandler.onTaskUpdated(getId(), getTask());
		}
	}


	public String getId() {
		return mDocument.getId();
	}


	public RapidDocument<Task> getDocument() {
		return mDocument;
	}


	public List<Tag> getTags() {
		return mTags;
	}
}

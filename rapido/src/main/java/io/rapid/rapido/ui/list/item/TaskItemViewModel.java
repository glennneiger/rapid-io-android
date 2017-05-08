package io.rapid.rapido.ui.list.item;


import io.rapid.RapidDocument;
import io.rapid.rapido.data.model.Task;


public class TaskItemViewModel {
	private final RapidDocument<Task> mDocument;
	private TaskItemHandler mHandler;


	public TaskItemViewModel(RapidDocument<Task> document, TaskItemHandler handler) {
		mDocument = document;
		mHandler = handler;
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
}

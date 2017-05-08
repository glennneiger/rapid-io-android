package io.rapid.rapido.ui.list;


import io.rapid.rapido.model.Task;


public class TaskItemViewModel {
	private final String mId;
	private Task mTask;
	private TaskItemHandler mHandler;


	public TaskItemViewModel(String id, Task task, TaskItemHandler handler) {
		mId = id;
		mTask = task;
		mHandler = handler;
	}


	public Task getTask() {
		return mTask;
	}


	public void edit() {
		mHandler.editTask(mId, mTask);
	}


	public void onCheckedChanged(boolean checked) {
		if(mTask.isDone() != checked) {
			mTask.setDone(checked);
			mHandler.onTaskUpdated(mId, mTask);
		}
	}


	public String getId() {
		return mId;
	}
}

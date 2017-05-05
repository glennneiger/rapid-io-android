package io.rapid.rapido;


import android.databinding.ObservableField;

import io.rapid.rapido.model.Task;


public class EditTaskViewModel {
	public final ObservableField<Task> task = new ObservableField<>();
	private String mTaskId;

	private EditTaskHandler mEditTaskHandler;


	public interface EditTaskHandler {
		void onTaskUpdated(String taskId, Task task);
	}


	public EditTaskViewModel(String taskId, Task task, EditTaskHandler editTaskHandler) {
		mEditTaskHandler = editTaskHandler;
		this.task.set(task != null ? task : new Task());
		mTaskId = taskId;
	}


	public void onTaskUpdated() {
		mEditTaskHandler.onTaskUpdated(mTaskId, task.get());
	}
}

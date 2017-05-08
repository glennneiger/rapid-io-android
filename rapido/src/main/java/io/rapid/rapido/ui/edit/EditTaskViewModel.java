package io.rapid.rapido.ui.edit;


import android.app.Dialog;
import android.databinding.ObservableField;

import io.rapid.rapido.model.Task;
import io.rapid.rapido.ui.list.TaskItemHandler;


public class EditTaskViewModel {
	public final ObservableField<Task> task = new ObservableField<>();
	private final Dialog mDialog;
	private String mTaskId;
	private TaskItemHandler mEditTaskHandler;


	public EditTaskViewModel(Dialog dialog, String taskId, Task task, TaskItemHandler editTaskHandler) {
		mDialog = dialog;
		mEditTaskHandler = editTaskHandler;
		this.task.set(task != null ? task : new Task());
		mTaskId = taskId;
	}


	public void onTaskUpdated() {
		mEditTaskHandler.onTaskUpdated(mTaskId, task.get());
		dismiss();
	}


	public void dismiss() {
		mDialog.dismiss();
	}
}

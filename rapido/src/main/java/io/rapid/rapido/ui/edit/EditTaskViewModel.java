package io.rapid.rapido.ui.edit;


import android.app.Dialog;
import android.databinding.ObservableField;

import java.util.List;
import java.util.Set;

import io.rapid.rapido.model.Tag;
import io.rapid.rapido.model.Task;
import io.rapid.rapido.ui.list.item.TaskItemHandler;


public class EditTaskViewModel {
	public final ObservableField<Task> task = new ObservableField<>();
	public final List<Tag> tags;

	private final Dialog mDialog;
	private String mTaskId;
	private TaskItemHandler mEditTaskHandler;


	public EditTaskViewModel(Dialog dialog, String taskId, Task task, List<Tag> tags, TaskItemHandler editTaskHandler) {
		mDialog = dialog;
		mEditTaskHandler = editTaskHandler;
		this.task.set(task != null ? task : new Task());
		mTaskId = taskId;
		this.tags = tags;
	}


	public void onTaskUpdated() {
		mEditTaskHandler.onTaskUpdated(mTaskId, task.get());
		dismiss();
	}


	public void dismiss() {
		mDialog.dismiss();
	}


	public void onSelectedTagsChanged(Set<String> selectedTags) {
		task.get().setTags(selectedTags);
	}
}

package io.rapid.rapido.ui.list;

import io.rapid.rapido.model.Task;


/**
 * View interface for TaskList screen
 */
public interface TaskListView {
	void showToast(String message);
	void showAddDialog();
	void showEditDialog(String taskId, Task task);
}

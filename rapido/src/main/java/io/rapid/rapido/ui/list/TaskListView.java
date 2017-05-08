package io.rapid.rapido.ui.list;

import io.rapid.rapido.data.model.Task;


public interface TaskListView {
	void showToast(String message);
	void showAddDialog();
	void showEditDialog(String taskId, Task task);
}

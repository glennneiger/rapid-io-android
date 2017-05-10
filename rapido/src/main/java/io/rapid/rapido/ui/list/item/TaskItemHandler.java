package io.rapid.rapido.ui.list.item;

import io.rapid.rapido.model.Task;


public interface TaskItemHandler {
	void deleteTask(String id);
	void editTask(String id, Task task);
	void onTaskUpdated(String id, Task task);
}

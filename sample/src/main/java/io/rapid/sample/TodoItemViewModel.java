package io.rapid.sample;


public class TodoItemViewModel {
	private final String mId;
	private Todo mTodo;
	private TodoItemHandler mHandler;


	public interface TodoItemHandler {
		void onDelete(String id, Todo todo);
		void onChange(String id, Todo todo);
	}


	public TodoItemViewModel(String id, Todo todo, TodoItemHandler handler) {
		mId = id;
		mTodo = todo;
		mHandler = handler;
	}


	public Todo getTodo() {
		return mTodo;
	}


	public void remove() {
		mHandler.onDelete(mId, mTodo);
	}


	public void onCheckedChanged(boolean checked) {
		if(mTodo.isChecked() != checked) {
			mTodo.setChecked(checked);
			mHandler.onChange(mId, mTodo);
		}
	}


	public String getId() {
		return mId;
	}
}

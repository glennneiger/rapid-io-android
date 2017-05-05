package io.rapid.rapido;


import android.databinding.BindingAdapter;
import android.graphics.Paint;
import android.widget.TextView;

import io.rapid.rapido.model.Task;


public class TaskItemViewModel {
	private final String mId;
	private Task mTask;
	private TaskItemHandler mHandler;


	public interface TaskItemHandler {
		void onDelete(String id, Task task);
		void onChange(String id, Task task);
		void onEdit(String id, Task task);
	}


	public TaskItemViewModel(String id, Task task, TaskItemHandler handler) {
		mId = id;
		mTask = task;
		mHandler = handler;
	}


	@BindingAdapter("strikethrough")
	public static void setStrikeThrough(TextView textView, boolean strikethrough) {
		if(strikethrough)
			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
	}


	public Task getTask() {
		return mTask;
	}


	public void remove() {
		mHandler.onDelete(mId, mTask);
	}


	public void edit() {
		mHandler.onEdit(mId, mTask);
	}


	public void onCheckedChanged(boolean checked) {
		if(mTask.isDone() != checked) {
			mTask.setDone(checked);
			mHandler.onChange(mId, mTask);
		}
	}


	public String getId() {
		return mId;
	}
}

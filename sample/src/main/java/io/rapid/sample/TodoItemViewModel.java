package io.rapid.sample;


import android.databinding.BindingAdapter;
import android.graphics.Paint;
import android.widget.TextView;


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


	@BindingAdapter("strikethrough")
	public static void setStrikeThrough(TextView textView, boolean strikethrough) {
		if(strikethrough)
			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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

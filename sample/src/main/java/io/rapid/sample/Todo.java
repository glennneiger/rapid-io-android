package io.rapid.sample;


public class Todo {
	private String mTitle;
	private boolean mChecked;


	public Todo() {
	}


	public Todo(String title) {
		mTitle = title;
	}


	@Override
	public String toString() {
		return "Todo: " + mTitle;
	}


	public String getTitle() {
		return mTitle;
	}


	public boolean isChecked() {
		return mChecked;
	}


	public void setChecked(boolean checked) {
		mChecked = checked;
	}
}

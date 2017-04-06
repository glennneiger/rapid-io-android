package io.rapid.sample;


import android.databinding.BaseObservable;
import android.databinding.Bindable;


public class Todo extends BaseObservable {
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


	@Bindable
	public boolean isChecked() {
		return mChecked;
	}


	public void setChecked(boolean checked) {
		mChecked = checked;
		notifyPropertyChanged(BR.checked);
	}
}

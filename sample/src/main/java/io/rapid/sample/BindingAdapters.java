package io.rapid.sample;


import android.databinding.BindingAdapter;
import android.view.View;


public class BindingAdapters {

	@BindingAdapter("show")
	public static void setShow(View view, boolean show) {
		view.setVisibility(show ? View.VISIBLE : View.GONE);
	}
}

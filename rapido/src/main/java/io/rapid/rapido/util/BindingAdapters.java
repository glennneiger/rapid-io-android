package io.rapid.rapido.util;


import android.databinding.BindingAdapter;
import android.graphics.Paint;
import android.view.View;
import android.widget.TextView;


public class BindingAdapters {

	public interface OnDoneListener {
		void onDone();
	}


	@BindingAdapter("show")
	public static void setShow(View view, boolean show) {
		view.setVisibility(show ? View.VISIBLE : View.GONE);
	}


	@BindingAdapter("strikethrough")
	public static void setStrikeThrough(TextView textView, boolean strikethrough) {
		if(strikethrough)
			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
	}
}

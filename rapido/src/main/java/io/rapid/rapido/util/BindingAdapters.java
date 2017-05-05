package io.rapid.rapido.util;


import android.databinding.BindingAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;


public class BindingAdapters {

	public interface OnDoneListener {
		void onDone();
	}


	@BindingAdapter("show")
	public static void setShow(View view, boolean show) {
		view.setVisibility(show ? View.VISIBLE : View.GONE);
	}


	@BindingAdapter("onDone")
	public static void setOnDoneListener(EditText editText, OnDoneListener listener) {
		if(listener != null) {
			editText.setOnEditorActionListener((v, actionId, event) -> {
				if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH
						|| (event != null && (event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
					listener.onDone();
					return true;
				}
				return false;
			});
		} else {
			editText.setOnEditorActionListener(null);
		}
	}
}

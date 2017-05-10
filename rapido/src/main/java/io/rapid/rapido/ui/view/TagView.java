package io.rapid.rapido.ui.view;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.Collection;

import io.rapid.rapido.model.Tag;
import io.rapid.rapido.databinding.ItemTagBinding;


public class TagView extends LinearLayout {

	public TagView(Context context) {
		super(context);
		init();
	}


	public TagView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	public TagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}


	public void setTags(Collection<Tag> tags) {
		removeAllViews();

		for(Tag tag : tags) {
			ItemTagBinding tagBinding = ItemTagBinding.inflate(LayoutInflater.from(getContext()), this, true);
			tagBinding.setTag(tag);
		}
	}


	private void init() {
		setOrientation(HORIZONTAL);
	}
}

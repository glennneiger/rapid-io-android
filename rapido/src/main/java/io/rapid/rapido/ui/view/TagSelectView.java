package io.rapid.rapido.ui.view;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.rapid.rapido.model.Tag;
import io.rapid.rapido.databinding.ItemTagSelectBinding;


public class TagSelectView extends LinearLayout {
	private Set<String> mSelection = new HashSet<>();
	private OnSelectionChangedListener mListener;
	private Map<String, ItemTagSelectBinding> mBindings;


	public interface OnSelectionChangedListener {
		void onSelectionChanged(Set<String> selectedTags);
	}


	public TagSelectView(Context context) {
		super(context);
		init();
	}


	public TagSelectView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	public TagSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}


	public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
		mListener = listener;
	}


	public void setSelection(Set<String> selectedTags) {
		mSelection = selectedTags != null ? selectedTags : new HashSet<>();
		if(mBindings != null) {
			for(ItemTagSelectBinding binding : mBindings.values()) {
				binding.setInitState(false);
			}

			if(selectedTags != null) {
				for(String selectedTag : selectedTags) {
					mBindings.get(selectedTag).setInitState(true);
				}
			}
		}
	}


	private void init() {
		setOrientation(VERTICAL);
		setTags(Tag.getAllTags());
	}


	private void setTags(Collection<Tag> tags) {
		removeAllViews();
		mBindings = new HashMap<>();

		for(Tag tag : tags) {
			ItemTagSelectBinding tagBinding = ItemTagSelectBinding.inflate(LayoutInflater.from(getContext()), this, true);
			tagBinding.setTag(tag);
			tagBinding.setInitState(mSelection.contains(tag.getName()));
			tagBinding.setCheckedChangedListener((buttonView, isChecked) -> {
				if(isChecked) {
					mSelection.add(tag.getName());
				} else {
					mSelection.remove(tag.getName());
				}
				onSelectionChanged();
			});
			mBindings.put(tag.getName(), tagBinding);
		}
	}


	private void onSelectionChanged() {
		if(mListener != null) {
			mListener.onSelectionChanged(mSelection);
		}
	}

}

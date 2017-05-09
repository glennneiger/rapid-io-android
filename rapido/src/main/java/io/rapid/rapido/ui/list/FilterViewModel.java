package io.rapid.rapido.ui.list;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.rapid.Sorting;
import io.rapid.rapido.BR;
import io.rapid.rapido.R;
import io.rapid.rapido.data.model.FilterState;
import io.rapid.rapido.data.model.Tag;


public class FilterViewModel extends BaseObservable {
	public final ObservableInt orderPosition = new ObservableInt();
	public final ObservableField<FilterState> filterState = new ObservableField<>();
	private final List<String> mOrderValues;
	private final List<String> mTagNames;
	public List<Boolean> filterTags;
	public List<Tag> tags;
	private String mOrderProperty;
	private Sorting mOrderSorting;
	private OnOrderChangedListener mOnOrderChangedListener;


	public interface OnOrderChangedListener {
		void onOrderChanged(String orderProperty, Sorting sorting, FilterState filterState, List<String> filterTags);
	}


	public FilterViewModel(Context context, String orderProperty, Sorting orderSorting, OnOrderChangedListener onOrderChangedListener) {
		mOrderSorting = orderSorting;
		mOnOrderChangedListener = onOrderChangedListener;
		mOrderValues = Arrays.asList(context.getResources().getStringArray(R.array.order_values));
		mOrderProperty = orderProperty;

		filterTags = new ArrayList<>();
		tags = new ArrayList<>();
		mTagNames = Arrays.asList(context.getResources().getStringArray(R.array.tag_names));
		List<String> tagColors = Arrays.asList(context.getResources().getStringArray(R.array.tag_colors));
		for(int i = 0; i < mTagNames.size(); i++) {
			tags.add(new Tag(mTagNames.get(i), Color.parseColor(tagColors.get(i))));
			filterTags.add(false);
		}
	}


	public int getOrderPosition() {
		return mOrderValues.indexOf(mOrderProperty);
	}


	public void setOrderPosition(int position) {
		mOrderProperty = mOrderValues.get(position);
		onFilterChanged();
	}


	public void toggleSorting() {
		setOrderSorting(mOrderSorting == Sorting.ASC ? Sorting.DESC : Sorting.ASC);
	}


	@Bindable
	public Sorting getOrderSorting() {
		return mOrderSorting;
	}


	public void setOrderSorting(Sorting sorting) {
		mOrderSorting = sorting;
		notifyPropertyChanged(BR.orderSorting);
		onFilterChanged();
	}


	public void setFilterState(FilterState filterState) {
		this.filterState.set(filterState);
		onFilterChanged();
	}


	private void onFilterChanged() {
		List<String> filterTagNames = new ArrayList<>();
		for(int i = 0; i < filterTags.size(); i++) {
			if(filterTags.get(i)) {
				filterTagNames.add(mTagNames.get(i));
			}
		}
		mOnOrderChangedListener.onOrderChanged(mOrderProperty, mOrderSorting, filterState.get(), filterTagNames);
	}
}

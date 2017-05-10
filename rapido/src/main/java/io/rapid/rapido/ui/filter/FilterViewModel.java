package io.rapid.rapido.ui.filter;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.rapid.Sorting;
import io.rapid.rapido.BR;
import io.rapid.rapido.R;


public class FilterViewModel extends BaseObservable {
	private final List<String> mOrderValues;
	private FilterState mFilterState = FilterState.ALL;
	private String mOrderProperty = "createdAt";
	private Sorting mOrderSorting = Sorting.DESC;
	private Set<String> mFilterTags;
	private OnFilterChangedListener mOnFilterChangedListener;


	public interface OnFilterChangedListener {
		void onFilterChanged(String orderProperty, Sorting sorting, FilterState filterState, Set<String> filterTags);
	}


	public FilterViewModel(Context context, OnFilterChangedListener onFilterChangedListener) {
		mOnFilterChangedListener = onFilterChangedListener;
		mOrderValues = Arrays.asList(context.getResources().getStringArray(R.array.order_values));

		mFilterTags = new HashSet<>();
		onFilterChanged();
	}


	public int getOrderPosition() {
		return mOrderValues.indexOf(mOrderProperty);
	}


	public void setOrderPosition(int position) {
		String orderProperty = mOrderValues.get(position);
		if(orderProperty.equals(mOrderProperty)) return;

		mOrderProperty = orderProperty;
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
		if(sorting == mOrderSorting) return;
		mOrderSorting = sorting;
		notifyPropertyChanged(BR.orderSorting);
		onFilterChanged();
	}


	@Bindable
	public FilterState getFilterState() {
		return mFilterState;
	}


	public void setFilterState(FilterState filterState) {
		if(filterState == mFilterState) return;

		mFilterState = filterState;
		notifyPropertyChanged(BR.filterState);
		onFilterChanged();
	}


	public void onSelectedTagsChanged(Set<String> selectedTags) {
		mFilterTags = selectedTags;
		onFilterChanged();
	}


	private void onFilterChanged() {
		mOnFilterChangedListener.onFilterChanged(mOrderProperty, mOrderSorting, mFilterState, mFilterTags);
	}
}

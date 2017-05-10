package io.rapid.rapido.ui.list;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.rapid.Sorting;
import io.rapid.rapido.BR;
import io.rapid.rapido.R;
import io.rapid.rapido.data.model.FilterState;
import io.rapid.rapido.data.model.Tag;


public class FilterViewModel extends BaseObservable {
	public final ObservableInt orderPosition = new ObservableInt();
	public final ObservableField<FilterState> filterState = new ObservableField<>();
	private final List<String> mOrderValues;
	public Set<String> filterTags;
	public List<Tag> tags;
	private String mOrderProperty;
	private Sorting mOrderSorting;
	private OnFilterChangedListener mOnFilterChangedListener;


	public interface OnFilterChangedListener {
		void onFilterChanged(String orderProperty, Sorting sorting, FilterState filterState, Set<String> filterTags);
	}


	public FilterViewModel(Context context, OnFilterChangedListener onFilterChangedListener) {
		mOrderSorting = Sorting.ASC;
		mOnFilterChangedListener = onFilterChangedListener;
		mOrderValues = Arrays.asList(context.getResources().getStringArray(R.array.order_values));
		mOrderProperty = "createdAt";

		filterState.set(FilterState.ALL);

		filterTags = new HashSet<>();
		tags = Tag.getAllTags();
		onFilterChanged();
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


	public void onSelectedTagsChanged(Set<String> selectedTags) {
		filterTags = selectedTags;
		onFilterChanged();
	}


	private void onFilterChanged() {
		mOnFilterChangedListener.onFilterChanged(mOrderProperty, mOrderSorting, filterState.get(), filterTags);
	}
}

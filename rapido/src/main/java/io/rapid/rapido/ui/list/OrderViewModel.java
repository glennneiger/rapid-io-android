package io.rapid.rapido.ui.list;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableInt;

import java.util.List;

import io.rapid.Sorting;
import io.rapid.rapido.BR;


public class OrderViewModel extends BaseObservable{
	private final List<String> mOrderValues;
	private String mOrderProperty;
	private Sorting mOrderSorting;
	private OnOrderChangedListener mOnOrderChangedListener;


	public interface OnOrderChangedListener {
		void onOrderChanged(String orderProperty, Sorting sorting);
	}


	public final ObservableInt orderPosition = new ObservableInt();


	public OrderViewModel(List<String> orderValues, String orderProperty, Sorting orderSorting, OnOrderChangedListener onOrderChangedListener) {
		mOrderSorting = orderSorting;
		mOnOrderChangedListener = onOrderChangedListener;
		mOrderValues = orderValues;
		mOrderProperty = orderProperty;
	}


	public int getOrderPosition() {
		return mOrderValues.indexOf(mOrderProperty);
	}


	public void setOrderPosition(int position) {
		mOrderProperty = mOrderValues.get(position);
		mOnOrderChangedListener.onOrderChanged(mOrderProperty, mOrderSorting);
	}


	public void toggleSorting() {
		setOrderSorting(mOrderSorting == Sorting.ASC ? Sorting.DESC : Sorting.ASC);
	}


	public void setOrderSorting(Sorting sorting) {
		mOrderSorting = sorting;
		notifyPropertyChanged(BR.orderSorting);
		mOnOrderChangedListener.onOrderChanged(mOrderProperty, mOrderSorting);
	}


	@Bindable
	public Sorting getOrderSorting() {
		return mOrderSorting;
	}
}

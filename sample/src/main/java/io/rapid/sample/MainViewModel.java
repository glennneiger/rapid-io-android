package io.rapid.sample;


import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class MainViewModel {
	public ItemBinding<Car> itemBinding = ItemBinding.of(io.rapid.sample.BR.car, R.layout.item_car);
	public DiffObservableList<Car> items = new DiffObservableList<>(new DiffObservableList.Callback<Car>() {
		@Override
		public boolean areItemsTheSame(Car oldItem, Car newItem) {
			return oldItem.getNumber() == newItem.getNumber();
		}


		@Override
		public boolean areContentsTheSame(Car oldItem, Car newItem) {
			return false;
		}
	});
}

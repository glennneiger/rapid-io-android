package io.rapid.sample;


import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class MainViewModel {
	public ItemBinding<CarItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_car);
	public DiffObservableList<CarItemViewModel> items = new DiffObservableList<>(new DiffObservableList.Callback<CarItemViewModel>() {
		@Override
		public boolean areItemsTheSame(CarItemViewModel oldItem, CarItemViewModel newItem) {
			return oldItem.getCar().getNumber() == newItem.getCar().getNumber();
		}


		@Override
		public boolean areContentsTheSame(CarItemViewModel oldItem, CarItemViewModel newItem) {
			return false;
		}
	});
}

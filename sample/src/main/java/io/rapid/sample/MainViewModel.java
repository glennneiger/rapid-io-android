package io.rapid.sample;


import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class MainViewModel {
	public ItemBinding<TodoItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_todo);
	public DiffObservableList<TodoItemViewModel> items = new DiffObservableList<>(new DiffObservableList.Callback<TodoItemViewModel>() {
		@Override
		public boolean areItemsTheSame(TodoItemViewModel oldItem, TodoItemViewModel newItem) {
			return oldItem.getId().equals(newItem.getId());
		}


		@Override
		public boolean areContentsTheSame(TodoItemViewModel oldItem, TodoItemViewModel newItem) {
			return false;
		}
	});
}

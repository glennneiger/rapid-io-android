package io.rapid.rapido.ui.list.item;


import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.rapid.rapido.databinding.ItemTaskBinding;


/**
 * RecyclerView adapter for dislplaying list of Tasks
 * Leveraging Android Data Binding for view binding
 * <p>
 * Items are wrapped within ItemViewModel
 */
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

	private List<TaskItemViewModel> mTaskItemViewModels = new ArrayList<>();


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ItemTaskBinding itemBinding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(itemBinding);
	}


	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.bind(mTaskItemViewModels.get(position));
	}


	@Override
	public int getItemCount() {
		return mTaskItemViewModels.size();
	}


	public List<TaskItemViewModel> getItems() {
		return mTaskItemViewModels;
	}


	public void setItems(List<TaskItemViewModel> items) {
		// use DiffUtil from Android support library to calculate list changes and perform animations
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
			@Override
			public int getOldListSize() {
				return getItemCount();
			}


			@Override
			public int getNewListSize() {
				return items.size();
			}


			@Override
			public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
				return getItems().get(oldItemPosition).getId().equals(items.get(newItemPosition).getId());
			}


			@Override
			public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
				return getItems().get(oldItemPosition).getDocument().hasSameContentAs(items.get(newItemPosition).getDocument());
			}
		});
		mTaskItemViewModels = items;
		diffResult.dispatchUpdatesTo(this);
	}


	class ViewHolder extends RecyclerView.ViewHolder {

		private final ItemTaskBinding mBinding;


		ViewHolder(ItemTaskBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
		}


		void bind(TaskItemViewModel taskItemViewModel) {
			mBinding.setViewModel(taskItemViewModel);
			mBinding.executePendingBindings();
		}
	}
}

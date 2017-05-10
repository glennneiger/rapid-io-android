package io.rapid.rapido.data.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.rapid.rapido.databinding.ItemTaskBinding;
import io.rapid.rapido.ui.list.item.TaskItemViewModel;


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


	public void setItems(List<TaskItemViewModel> taskItemViewModels) {
		mTaskItemViewModels = taskItemViewModels;
	}


	class ViewHolder extends RecyclerView.ViewHolder {

		private final ItemTaskBinding mBinding;


		ViewHolder(ItemTaskBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
		}


		void bind(TaskItemViewModel taskItemViewModel) {
			mBinding.setViewModel(taskItemViewModel);
		}
	}
}

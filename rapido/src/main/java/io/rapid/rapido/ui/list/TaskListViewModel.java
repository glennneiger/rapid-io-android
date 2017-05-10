package io.rapid.rapido.ui.list;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.rapid.ConnectionState;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocumentReference;
import io.rapid.Sorting;
import io.rapid.rapido.BR;
import io.rapid.rapido.Config;
import io.rapid.rapido.R;
import io.rapid.rapido.data.model.FilterState;
import io.rapid.rapido.data.model.Task;
import io.rapid.rapido.ui.list.item.TaskItemHandler;
import io.rapid.rapido.ui.list.item.TaskItemViewModel;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class TaskListViewModel implements TaskItemHandler, FilterViewModel.OnFilterChangedListener {
	public final ObservableField<String> searchQuery = new ObservableField<>();
	public final ObservableField<String> orderProperty = new ObservableField<>();
	public final ObservableField<Sorting> orderSorting = new ObservableField<>();
	public final ObservableField<FilterState> filterState = new ObservableField<>();
	public final ObservableField<Set<String>> filterTags = new ObservableField<>(new HashSet<>());
	public ObservableBoolean searching = new ObservableBoolean();
	public ObservableField<ConnectionState> connectionState = new ObservableField<>();
	public ItemBinding<TaskItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_task);
	public DiffObservableList<TaskItemViewModel> tasks = new DiffObservableList<>(new DiffObservableList.Callback<TaskItemViewModel>() {
		@Override
		public boolean areItemsTheSame(TaskItemViewModel oldItem, TaskItemViewModel newItem) {
			return oldItem.getId().equals(newItem.getId());
		}


		@Override
		public boolean areContentsTheSame(TaskItemViewModel oldItem, TaskItemViewModel newItem) {
			return oldItem.getDocument().hasSameContentAs(newItem.getDocument());
		}
	});
	private RapidCollectionSubscription mSubscription;
	private RapidCollectionReference<Task> mTasksReference;
	private TaskListView mView;


	@Override
	public void deleteTask(String id) {
		mTasksReference.document(id).delete()
				.onError(error -> mView.showToast(error.getMessage()));
	}


	@Override
	public void onTaskUpdated(String id, Task task) {
		RapidDocumentReference<Task> doc;
		if(id != null) {
			doc = mTasksReference.document(id);
		} else {
			doc = mTasksReference.newDocument();
			task.setCreatedAt(new Date());
		}
		doc.mutate(task).onError(error -> mView.showToast(error.getMessage()));
	}


	@Override
	public void editTask(String id, Task task) {
		mView.showEditDialog(id, task);
	}


	@Override
	public void onFilterChanged(String orderProperty, Sorting sorting, FilterState filterState, Set<String> filterTags) {
		this.orderProperty.set(orderProperty);
		this.orderSorting.set(sorting);
		this.filterState.set(filterState);
		this.filterTags.set(filterTags);

		if(mSubscription != null) {
			mSubscription.unsubscribe();
			subscribe();
		}
	}


	public void initialize(TaskListView view) {
		mView = view;

		Rapid.getInstance().authorize(Config.RAPID_AUTH_TOKEN);

		Rapid.getInstance().addConnectionStateListener(connectionState::set);

		mTasksReference = Rapid.getInstance().collection(Config.TODO_COLLECTION_NAME, Task.class);
	}


	public void onViewAttached() {
		subscribe();
	}


	public void onViewDetached() {
		Rapid.getInstance().removeAllConnectionStateListeners();
		mSubscription.unsubscribe();
	}


	public void deleteTask(int position) {
		TaskItemViewModel taskToDelete = tasks.get(position);
		deleteTask(taskToDelete.getId());
	}


	private void subscribe() {

		// if search query is not empty - add it as a filter
		String query = searchQuery.get();
		if(query != null && !query.isEmpty()) {
			mTasksReference.beginOr()
					.contains("title", query)
					.contains("description", query)
					.endOr();
		}

		for(String tag : filterTags.get()) {
			mTasksReference.arrayContains("tags", tag);
		}

		if(filterState.get() == FilterState.DONE) {
			mTasksReference.equalTo("done", true);
		} else if(filterState.get() == FilterState.NOT_DONE) {
			mTasksReference.equalTo("done", false);
		}

		// create subscription
		mSubscription = mTasksReference
				.orderBy(orderProperty.get(), orderSorting.get())
				.map(document -> new TaskItemViewModel(document, this))
				.subscribe(items -> tasks.update(items))
				.onError(error -> {
					error.printStackTrace();
					mView.showToast(error.getMessage());
				});
	}


}

package io.rapid.rapido.ui.list;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import java.util.Date;
import java.util.Set;

import io.rapid.ConnectionState;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocumentReference;
import io.rapid.Sorting;
import io.rapid.rapido.Config;
import io.rapid.rapido.data.adapter.TaskListAdapter;
import io.rapid.rapido.data.model.Task;
import io.rapid.rapido.ui.filter.FilterState;
import io.rapid.rapido.ui.filter.FilterViewModel;
import io.rapid.rapido.ui.list.item.TaskItemHandler;
import io.rapid.rapido.ui.list.item.TaskItemViewModel;


public class TaskListViewModel implements TaskItemHandler, FilterViewModel.OnFilterChangedListener {
	public final ObservableField<String> searchQuery = new ObservableField<>();
	public ObservableBoolean searching = new ObservableBoolean();
	public ObservableField<ConnectionState> connectionState = new ObservableField<>();
	public TaskListAdapter taskListAdapter = new TaskListAdapter();

	private Sorting mOrderSorting;
	private String mOrderProperty;
	private FilterState mFilterState;
	private Set<String> mFilterTags;
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
		mOrderProperty = orderProperty;
		mOrderSorting = sorting;
		mFilterState = filterState;
		mFilterTags = filterTags;

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
		TaskItemViewModel taskToDelete = taskListAdapter.getItems().get(position);
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

		if(mFilterTags != null) {
			for(String tag : mFilterTags) {
				mTasksReference.arrayContains("tags", tag);
			}
		}

		if(mFilterState == FilterState.DONE)
			mTasksReference.equalTo("done", true);
		else if(mFilterState == FilterState.NOT_DONE)
			mTasksReference.equalTo("done", false);

		// create subscription
		mSubscription = mTasksReference
				.orderBy(mOrderProperty, mOrderSorting)
				.map(document -> new TaskItemViewModel(document, this))
				.subscribeWithListUpdates((items, listUpdates) -> {
					taskListAdapter.setItems(items);
					listUpdates.dispatchUpdatesTo(taskListAdapter);
				})
				.onError(error -> {
					error.printStackTrace();
					mView.showToast(error.getMessage());
				});
	}


}

package io.rapid.rapido.ui.list;


import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import java.util.Date;

import io.rapid.ConnectionState;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocumentReference;
import io.rapid.Sorting;
import io.rapid.rapido.BR;
import io.rapid.rapido.Config;
import io.rapid.rapido.R;
import io.rapid.rapido.model.Task;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class TaskListViewModel implements TaskItemHandler {
	public final ObservableField<String> newTaskTitle = new ObservableField<>();
	public final ObservableField<String> searchQuery = new ObservableField<>();
	public final ObservableField<String> orderProperty = new ObservableField<>("done");
	public final ObservableField<Sorting> orderSorting = new ObservableField<>(Sorting.ASC);
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
			return true;
		}
	});

	private RapidCollectionSubscription mSubscription;
	private RapidCollectionReference<Task> mTasks;
	private TaskListActivity mActivity;


	@Override
	public void deleteTask(String id) {
		mTasks.document(id).delete()
				.onError(error -> mActivity.showToast(error.getMessage()));
	}


	@Override
	public void onTaskUpdated(String id, Task task) {
		RapidDocumentReference<Task> doc;
		if(id != null) {
			doc = mTasks.document(id);
		} else {
			doc = mTasks.newDocument();
			task.setCreatedAt(new Date());
		}
		doc.mutate(task).onError(error -> mActivity.showToast(error.getMessage()));
	}


	@Override
	public void editTask(String id, Task task) {
		mActivity.showEditDialog(id, task);
	}


	public void initialize(TaskListActivity taskListActivity) {
		mActivity = taskListActivity;

		Rapid.getInstance().authorize(Config.RAPID_AUTH_TOKEN);

		Rapid.getInstance().addConnectionStateListener(connectionState::set);

		mTasks = Rapid.getInstance().collection("tasks_android_demo_01", Task.class);

		Observable.OnPropertyChangedCallback resubscribeCallback = new Observable.OnPropertyChangedCallback() {
			@Override
			public void onPropertyChanged(Observable observable, int i) {
				mSubscription.unsubscribe();
				subscribe();
			}
		};
		searchQuery.addOnPropertyChangedCallback(resubscribeCallback);
		orderProperty.addOnPropertyChangedCallback(resubscribeCallback);
		orderSorting.addOnPropertyChangedCallback(resubscribeCallback);
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
			mTasks.beginOr()
					.contains("title", query)
					.contains("description", query)
					.endOr();
		}

		// create subscription
		mSubscription = mTasks
				.orderBy(orderProperty.get(), orderSorting.get())
				.map(document -> new TaskItemViewModel(document.getId(), document.getBody(), this))
				.subscribe(items -> tasks.update(items))
				.onError(error -> {
					error.printStackTrace();
					mActivity.showToast(error.getMessage());
				});
	}


}

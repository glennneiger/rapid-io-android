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
import io.rapid.rapido.data.SettingsStorage;
import io.rapid.rapido.data.model.Task;
import io.rapid.rapido.ui.list.item.TaskItemHandler;
import io.rapid.rapido.ui.list.item.TaskItemViewModel;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class TaskListViewModel implements TaskItemHandler {
	public final ObservableField<String> searchQuery = new ObservableField<>();
	public final ObservableField<String> orderProperty = new ObservableField<>();
	public final ObservableField<Sorting> orderSorting = new ObservableField<>();
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


	public void initialize(TaskListView view, SettingsStorage settingsStorage) {
		mView = view;
		orderProperty.set(settingsStorage.getOrderProperty());
		orderSorting.set(settingsStorage.getOrderSorting());

		Rapid.getInstance().authorize(Config.RAPID_AUTH_TOKEN);

		Rapid.getInstance().addConnectionStateListener(connectionState::set);

		mTasksReference = Rapid.getInstance().collection("tasks_android_demo_01", Task.class);

		Observable.OnPropertyChangedCallback queryChangedCallback = new Observable.OnPropertyChangedCallback() {
			@Override
			public void onPropertyChanged(Observable observable, int i) {
				settingsStorage.setOrderProperty(orderProperty.get());
				settingsStorage.setOrderSorting(orderSorting.get());
				mSubscription.unsubscribe();
				subscribe();
			}
		};
		searchQuery.addOnPropertyChangedCallback(queryChangedCallback);
		orderProperty.addOnPropertyChangedCallback(queryChangedCallback);
		orderSorting.addOnPropertyChangedCallback(queryChangedCallback);
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

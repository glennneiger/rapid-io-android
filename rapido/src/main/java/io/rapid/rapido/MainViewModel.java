package io.rapid.rapido;


import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.widget.Toast;

import io.rapid.ConnectionState;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocumentReference;
import io.rapid.rapido.model.Task;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.collections.DiffObservableList;


public class MainViewModel implements TaskItemViewModel.TaskItemHandler, EditTaskViewModel.EditTaskHandler {
	public final ObservableField<String> newTaskTitle = new ObservableField<>();
	public final ObservableField<String> searchQuery = new ObservableField<>();
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
			return oldItem.getTask().equals(newItem.getTask());
		}
	});

	private RapidCollectionSubscription mSubscription;
	private RapidCollectionReference<Task> mTasks;
	private MainActivity mActivity;


	@Override
	public void onDelete(String id, Task task) {
		mTasks.document(id).delete()
				.onError(error -> showToast(error.getMessage()));
	}


	@Override
	public void onChange(String id, Task task) {
		mTasks.document(id).mutate(task);
	}


	@Override
	public void onEdit(String id, Task task) {
		mActivity.showEditDialog(id, task);
	}


	@Override
	public void onTaskUpdated(String taskId, Task task) {
		RapidDocumentReference<Task> doc = taskId != null ? mTasks.document(taskId) : mTasks.newDocument();
		doc.mutate(task).onError(error -> showToast(error.getMessage()));
	}


	public void initialize(MainActivity mainActivity) {
		mActivity = mainActivity;

		Rapid.getInstance().authorize(Config.RAPID_AUTH_TOKEN);

		Rapid.getInstance().addConnectionStateListener(connectionState::set);

		mTasks = Rapid.getInstance().collection("tasks_android_demo_01", Task.class);

		searchQuery.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
			@Override
			public void onPropertyChanged(Observable observable, int i) {
				// search query changed -> resubscribe
				mSubscription.unsubscribe();
				subscribe();
			}
		});
	}


	public void onViewAttached() {
		subscribe();
	}


	public void onViewDetached() {
		Rapid.getInstance().removeAllConnectionStateListeners();
		mSubscription.unsubscribe();
	}


	private void showToast(String message) {
		Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
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
				.orderBy("done")
				.map(document -> new TaskItemViewModel(document.getId(), document.getBody(), this))
				.subscribe(items -> tasks.update(items))
				.onError(error -> {
					error.printStackTrace();
					showToast(error.getMessage());
				});
	}


}

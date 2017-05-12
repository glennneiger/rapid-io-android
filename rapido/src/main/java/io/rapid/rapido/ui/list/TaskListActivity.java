package io.rapid.rapido.ui.list;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.rapid.rapido.R;
import io.rapid.rapido.databinding.ActivityTaskListBinding;
import io.rapid.rapido.databinding.DialogEditTaskBinding;
import io.rapid.rapido.databinding.DrawerFilterBinding;
import io.rapid.rapido.model.Tag;
import io.rapid.rapido.model.Task;
import io.rapid.rapido.ui.edit.EditTaskViewModel;
import io.rapid.rapido.ui.filter.FilterViewModel;


public class TaskListActivity extends AppCompatActivity implements TaskListView {

	private ActivityTaskListBinding mBinding;
	private TaskListViewModel mViewModel;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup ViewModel
		mViewModel = new TaskListViewModel();

		// setup binding
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_list);
		mBinding.setView(this);
		mBinding.setViewModel(mViewModel);

		// setup Toolbar
		setSupportActionBar(mBinding.toolbar);

		// setup filter drawer
		DrawerFilterBinding drawerBinding = DrawerFilterBinding.inflate(LayoutInflater.from(this), mBinding.drawerContent, true);
		FilterViewModel filterViewModel = new FilterViewModel(this, mViewModel);
		drawerBinding.setViewModel(filterViewModel);

		// setup swipe to delete on list items
		initListItemTouchHelper();

		// init ViewModel
		mViewModel.initialize(this);
	}


	@Override
	protected void onStart() {
		super.onStart();

		mViewModel.onViewAttached();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_search:
				TransitionManager.beginDelayedTransition((ViewGroup) mBinding.getRoot());
				mViewModel.searching.set(!mViewModel.searching.get());
				return true;
			case R.id.menu_filter:
				showFilter();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}


	@Override
	public void showAddDialog() {
		showEditDialog(null, null);
	}


	@Override
	public void showEditDialog(String taskId, Task task) {
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		DialogEditTaskBinding dialogBinding = DialogEditTaskBinding.inflate(LayoutInflater.from(this));

		EditTaskViewModel editTaskViewModel = new EditTaskViewModel(dialog, taskId, task, Tag.getAllTags(), mViewModel);

		dialogBinding.setViewModel(editTaskViewModel);
		dialog.setContentView(dialogBinding.getRoot());
		BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) dialogBinding.getRoot().getParent());
		bottomSheetBehavior.setSkipCollapsed(true);
		dialog.show();
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
	}


	@Override
	protected void onStop() {
		mViewModel.onViewDetached();

		super.onStop();
	}


	private void showFilter() {
		mBinding.drawerLayout.openDrawer(Gravity.END);
	}


	private void initListItemTouchHelper() {
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
			@Override
			public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
				return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
			}


			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}


			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				mViewModel.deleteTaskAtPosition(viewHolder.getAdapterPosition());
			}
		});
		itemTouchHelper.attachToRecyclerView(mBinding.list);
	}


}

package io.rapid.rapido;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import io.rapid.rapido.databinding.ActivityMainBinding;
import io.rapid.rapido.databinding.DialogAddTaskBinding;
import io.rapid.rapido.model.Task;


public class MainActivity extends AppCompatActivity {


	private ActivityMainBinding mBinding;
	private MainViewModel mViewModel;


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup ViewModel
		mViewModel = new MainViewModel();

		// setup views
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		setSupportActionBar(mBinding.toolbar);

		mBinding.setView(this);
		mBinding.setViewModel(mViewModel);

		mViewModel.initialize(this);
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
				mViewModel.searching.set(!mViewModel.searching.get());
				return true;
			case R.id.menu_filter:
				// TODO
				return true;
			case R.id.menu_order:
				// TODO
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onDestroy() {
		mViewModel.onViewDetached();
		super.onDestroy();
	}


	public void showAddDialog(){
		showEditDialog(null, null);
	}

	public void showEditDialog(String taskId, Task task) {
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this));

		EditTaskViewModel editTaskViewModel = new EditTaskViewModel(taskId, task, (tId, t) -> {
			dialog.dismiss();
			mViewModel.onTaskUpdated(tId, t);
		});

		dialogBinding.setViewModel(editTaskViewModel);
		dialog.setContentView(dialogBinding.getRoot());
		dialog.show();
	}


}

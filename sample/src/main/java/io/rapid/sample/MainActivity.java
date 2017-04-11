package io.rapid.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import java.util.UUID;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.Sorting;
import io.rapid.sample.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements TodoItemViewModel.TodoItemHandler {

	private RapidCollectionSubscription mSubscription;
	private ActivityMainBinding mBinding;
	private MainViewModel mViewModel;
	private RapidCollectionReference<Todo> mTodos;


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = new MainViewModel();
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		setSupportActionBar(mBinding.toolbar);
		mBinding.setViewModel(mViewModel);
		mBinding.newTodoTitle.setOnEditorActionListener((v, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				String text = mBinding.newTodoTitle.getText().toString();
				if(!text.isEmpty())
					addTodo(text);
				mBinding.newTodoTitle.setText("");
				return true;
			}
			return false;
		});

		Rapid.getInstance().addConnectionStateListener(state -> {
			mViewModel.connectionState.set(state);
			log(state.toString());
		});

		mTodos = Rapid.getInstance().collection("todos_xyz", Todo.class);

		mSubscription = mTodos
				.equalTo("receiver", "carl01")
				.beginOr()
				.equalTo("sender", "john123")
				.greaterOrEqualThan("urgency", 1)
				.endOr()
				.orderBy("sentDate", Sorting.DESC)
				.orderBy("urgency", Sorting.ASC)
				.map(document -> new TodoItemViewModel(document.getId(), document.getBody(), MainActivity.this))
				.subscribeWithListUpdates((items, listUpdate) -> {
					log(listUpdate.toString());
					mViewModel.items.update(items);
				})
				.onError(error -> Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_LONG).show());
	}


	@Override
	protected void onDestroy() {
		Rapid.getInstance().removeAllConnectionStateListeners();
		mSubscription.unsubscribe();
		super.onDestroy();
	}


	@Override
	public void onDelete(String id, Todo todo) {
		mTodos.document(id).delete()
				.onSuccess(() -> log("Deleted"))
				.onError(error -> Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_LONG).show());
	}


	@Override
	public void onChange(String id, Todo todo) {
		mTodos.document(id).mutate(todo);
	}


	public void addRandomItem(View view) {
		addTodo(UUID.randomUUID().toString().substring(0, 12));
	}


	private void addTodo(String title) {
		Todo todo = new Todo(title);
		mTodos.newDocument().mutate(todo)
				.onError(error -> Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_LONG).show());
	}
}

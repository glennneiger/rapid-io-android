package io.rapid.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.Sorting;
import io.rapid.sample.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements TodoItemViewModel.TodoItemHandler {

	private static final String RAPID_API_KEY = "sdafh87923jweql2393rfksad";
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
		mBinding.setViewModel(mViewModel);

		Rapid.initialize(RAPID_API_KEY);

		Rapid.getInstance().addConnectionStateListener(state -> log(state.toString()));

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
				.subscribe(items -> mViewModel.items.update(items));
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


	public void addItem(View view) {
		final EditText input = new EditText(this);
		input.setHint(R.string.add_hint);
		input.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS);

		new AlertDialog.Builder(this)
				.setTitle(R.string.add_title)
				.setView(input)
				.setPositiveButton(R.string.add, (dialog, whichButton) -> {
					String title = input.getText().toString();
					addTodo(title);
				})
				.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
				})
				.show();
	}


	private void addTodo(String title) {
		Todo todo = new Todo(title);
		mTodos.newDocument().mutate(todo)
				.onError(error -> Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_LONG).show());
	}
}

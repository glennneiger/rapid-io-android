package io.rapid.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import java.util.UUID;

import io.rapid.LogLevel;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocumentExecutor;
import io.rapid.sample.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements TodoItemViewModel.TodoItemHandler {

	private RapidCollectionSubscription mSubscription;
	private ActivityMainBinding mBinding;
	private MainViewModel mViewModel;
	private RapidCollectionReference<Todo> mTodos;
	private MenuItem mToggleSubscriptionMenu;
	private MenuItem mToggleAuthMenu;
	private String mSearchQuery;


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		enableStrictMode();
		Rapid.getInstance().setLogLevel(LogLevel.LOG_LEVEL_NONE);

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

//		auth();

		Rapid.getInstance().addConnectionStateListener(state -> {
			mViewModel.connectionState.set(state);
			log(state.toString());
		});

		mTodos = Rapid.getInstance().collection("todos_01", Todo.class);
//		subscribe();

		mBinding.searchField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}


			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mSearchQuery = s.toString();
				unsubscribe();
				subscribe();
			}


			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		mToggleSubscriptionMenu = menu.findItem(R.id.menu_toggle_subscription);
		mToggleAuthMenu = menu.findItem(R.id.menu_toggle_authentication);
		mToggleSubscriptionMenu.setTitle(R.string.unsubscribe);
		mToggleSubscriptionMenu.setIcon(R.drawable.ic_cloud_off);
		mToggleAuthMenu.setTitle(R.string.deauth);
		mToggleAuthMenu.setIcon(R.drawable.ic_deauth);
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_toggle_subscription:
				if(mSubscription != null && mSubscription.isSubscribed()) unsubscribe();
				else subscribe();
				return true;
			case R.id.menu_toggle_authentication:
				if(Rapid.getInstance().isAuthenticated()) deauth();
				else auth();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	protected void onDestroy() {
		Rapid.getInstance().removeAllConnectionStateListeners();
		unsubscribe();
		super.onDestroy();
	}


	@Override
	public void onDelete(String id, Todo todo) {
		mTodos.document(id).execute(oldDocument -> RapidDocumentExecutor.delete())
				.onSuccess(() -> log("Deleted"))
				.onError(error -> Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show());
	}


	@Override
	public void onChange(String id, Todo todo) {
		mTodos.document(id).mutate(todo);
	}


	public void addRandomItem(View view) {
		addTodo(UUID.randomUUID().toString().substring(0, 12));
	}


	private void enableStrictMode() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.penaltyDeath()
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.penaltyDeath()
				.build());
	}


	private void auth() {
		if(mToggleAuthMenu != null) {
			mToggleAuthMenu.setTitle(R.string.deauth);
			mToggleAuthMenu.setIcon(R.drawable.ic_deauth);
		}

		Rapid.getInstance().authorize(Config.MASTER_AUTH_TOKEN)
				.onSuccess(() -> log("Auth success"))
				.onError(error -> log("Auth fail: " + error.getMessage()));

		Rapid.getInstance().authorize(Config.MASTER_AUTH_TOKEN)
				.onSuccess(() -> log("Auth success"))
				.onError(error -> log("Auth fail: " + error.getMessage()));
	}


	private void deauth() {
		if(mToggleAuthMenu != null) {
			mToggleAuthMenu.setTitle(R.string.auth);
			mToggleAuthMenu.setIcon(R.drawable.ic_auth);
		}

		Rapid.getInstance().deauthorize()
				.onSuccess(() -> log("Deauth success"))
				.onError(error -> log("Deauth fail: " + error.getType().getName()));
	}


	private void subscribe() {
		mTodos.clearFilter();
		if(mToggleSubscriptionMenu != null) {
			mToggleSubscriptionMenu.setTitle(R.string.unsubscribe);
			mToggleSubscriptionMenu.setIcon(R.drawable.ic_cloud_off);
		}
		if(mSearchQuery != null && !mSearchQuery.isEmpty()) {
			mTodos.contains("mTitle", mSearchQuery);
		}
		mSubscription = mTodos
				.orderBy("mChecked")
				.map(document -> new TodoItemViewModel(document.getId(), document.getBody(), MainActivity.this))
				.subscribeWithListUpdates((items, listUpdate) -> {
					log(listUpdate.toString());
					mViewModel.items.update(items);
				})
				.onError(error -> {
					error.printStackTrace();
					Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
					if(mToggleSubscriptionMenu != null) {
						mToggleSubscriptionMenu.setTitle(R.string.subscribe);
						mToggleSubscriptionMenu.setIcon(R.drawable.ic_cloud);
					}
				});
	}


	private void unsubscribe() {
		if(mToggleSubscriptionMenu != null) {
			mToggleSubscriptionMenu.setTitle(R.string.subscribe);
			mToggleSubscriptionMenu.setIcon(R.drawable.ic_cloud);
		}
		if(mSubscription != null) mSubscription.unsubscribe();
	}


	private void addTodo(String title) {
		Todo todo = new Todo(title);
		mTodos.newDocument().mutate(todo)
				.onError(error -> Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show());
	}
}

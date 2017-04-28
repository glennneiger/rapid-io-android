package io.rapid.sample;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import java.util.UUID;

import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.sample.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements TodoItemViewModel.TodoItemHandler {

	private final String AUTH_TOKEN =
			"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbklkIjoidGVzdDIiLCJydWxlcyI6eyJ0b2Rvc18wMSI6eyJyZWFkIjp0cnVlLCJ3cml0ZSI6dHJ1ZX19fQ.0UfGIR7p2bLKfhqP6yZCA5BpCxBlfOshzhIbS_7t2qM";
	private RapidCollectionSubscription mSubscription;
	private ActivityMainBinding mBinding;
	private MainViewModel mViewModel;
	private RapidCollectionReference<Todo> mTodos;
	private MenuItem mToggleSubscriptionMenu;
	private MenuItem mToggleAuthMenu;


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableStrictMode();

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

		auth();

		Rapid.getInstance().addConnectionStateListener(state -> {
			mViewModel.connectionState.set(state);
			log(state.toString());
		});

		mTodos = Rapid.getInstance().collection("todos_01", Todo.class);
		subscribe();
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
		Drawable iconSub = VectorDrawableCompat.create(getResources(), R.drawable.ic_cloud_off, null);
		mToggleSubscriptionMenu.setTitle(R.string.unsubscribe);
		mToggleSubscriptionMenu.setIcon(iconSub);
		Drawable iconAuth = VectorDrawableCompat.create(getResources(), R.drawable.ic_unauth, null);
		mToggleAuthMenu.setTitle(R.string.unauth);
		mToggleAuthMenu.setIcon(iconAuth);
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_toggle_subscription:
				if(mSubscription.isSubscribed()) unsubscribe();
				else subscribe();
				return true;
			case R.id.menu_toggle_authentication:
				if(Rapid.getInstance().isAuthenticated()) unauth();
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
		mTodos.document(id).delete()
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


	private void auth()
	{
		if(mToggleAuthMenu != null) {
			Drawable icon = VectorDrawableCompat.create(getResources(), R.drawable.ic_unauth, null);
			mToggleAuthMenu.setTitle(R.string.unauth);
			mToggleAuthMenu.setIcon(icon);
		}

		Rapid.getInstance().authorize(AUTH_TOKEN)
				.onSuccess(() -> log("Auth success"))
				.onError(error -> log("Auth fail: " + error.getMessage()));
	}


	private void unauth()
	{
		if(mToggleAuthMenu != null) {
			Drawable icon = VectorDrawableCompat.create(getResources(), R.drawable.ic_auth, null);
			mToggleAuthMenu.setTitle(R.string.auth);
			mToggleAuthMenu.setIcon(icon);
		}

		Rapid.getInstance().unauthorize()
				.onSuccess(() -> log("Unauth success"))
				.onError(error -> log("Unauth fail: " + error.getType().getName()));
	}


	private void subscribe() {
		if(mToggleSubscriptionMenu != null) {
			Drawable icon = VectorDrawableCompat.create(getResources(), R.drawable.ic_cloud_off, null);
			mToggleSubscriptionMenu.setTitle(R.string.unsubscribe);
			mToggleSubscriptionMenu.setIcon(icon);
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
						Drawable icon = VectorDrawableCompat.create(getResources(), R.drawable.ic_cloud, null);
						mToggleSubscriptionMenu.setTitle(R.string.subscribe);
						mToggleSubscriptionMenu.setIcon(icon);
					}
				});
	}


	private void unsubscribe() {
		if(mToggleSubscriptionMenu != null) {
			Drawable icon = VectorDrawableCompat.create(getResources(), R.drawable.ic_cloud, null);
			mToggleSubscriptionMenu.setTitle(R.string.subscribe);
			mToggleSubscriptionMenu.setIcon(icon);
		}
		mSubscription.unsubscribe();
	}


	private void addTodo(String title) {
		Todo todo = new Todo(title);
		mTodos.newDocument().mutate(todo)
				.onError(error -> Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show());
	}
}

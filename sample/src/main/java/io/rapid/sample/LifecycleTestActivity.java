package io.rapid.sample;


import android.arch.lifecycle.LifecycleActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import io.rapid.Rapid;
import io.rapid.lifecycle.RapidLiveData;


public class LifecycleTestActivity extends LifecycleActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new View(this));

		Rapid.getInstance().authorize(Config.MASTER_AUTH_TOKEN);
		RapidLiveData.from(Rapid.getInstance().collection("android_lifecycle_test_001")).observe(this, rapidDocuments -> {
			Log.d("lifecycle", rapidDocuments.toString());
		});
	}
}

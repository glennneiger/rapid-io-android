package io.rapid.lifecycle;


import android.arch.lifecycle.LiveData;

import java.util.List;

import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocument;


public class RapidLiveData<T> extends LiveData<List<RapidDocument<T>>> {

	private RapidCollectionReference<T> mRapidCollectionReference;
	private RapidCollectionSubscription mSubscription;


	public static <T> RapidLiveData<T> from(RapidCollectionReference<T> collectionReference) {
		return new RapidLiveData<T>(collectionReference);
	}


	private RapidLiveData(RapidCollectionReference<T> rapidCollectionReference) {
		mRapidCollectionReference = rapidCollectionReference;
	}


	@Override
	protected void onActive() {
		mSubscription = mRapidCollectionReference.subscribe(list -> setValue(list));
	}


	@Override
	protected void onInactive() {
		mSubscription.unsubscribe();
	}
}

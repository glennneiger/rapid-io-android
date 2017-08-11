package io.rapid.lifecycle;


import android.arch.lifecycle.LiveData;

import java.util.List;

import io.rapid.RapidCollectionMapReference;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocument;
import io.rapid.RapidDocumentReference;
import io.rapid.RapidDocumentSubscription;


public class RapidLiveData {
	public static <T> LiveData<RapidDocument<T>> from(final RapidDocumentReference<T> documentReference) {
		LiveData<RapidDocument<T>> liveData = new LiveData<RapidDocument<T>>() {
			private RapidDocumentSubscription<T> mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(document -> setValue(document));
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


	public static <T> LiveData<List<RapidDocument<T>>> from(final RapidCollectionReference<T> documentReference) {
		LiveData<List<RapidDocument<T>>> liveData = new LiveData<List<RapidDocument<T>>>() {
			private RapidCollectionSubscription mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(rapidDocuments -> setValue(rapidDocuments));
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


	public static <T, S> LiveData<List<S>> from(final RapidCollectionMapReference<T, S> documentReference) {
		LiveData<List<S>> liveData = new LiveData<List<S>>() {
			private RapidCollectionSubscription mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(rapidDocuments -> setValue(rapidDocuments));
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


}

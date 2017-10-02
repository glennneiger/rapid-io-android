package io.rapid.lifecycle;


import android.arch.lifecycle.LiveData;

import java.util.List;

import io.rapid.RapidCallback;
import io.rapid.RapidCollectionMapReference;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidCollectionSubscription;
import io.rapid.RapidDocument;
import io.rapid.RapidDocumentMapReference;
import io.rapid.RapidDocumentReference;
import io.rapid.RapidDocumentSubscription;


public class RapidLiveData {
	public static <T> LiveData<RapidDocument<T>> from(final RapidDocumentReference<T> documentReference) {
		return from(documentReference, Throwable::printStackTrace);
	}


	public static <T> LiveData<RapidDocument<T>> from(final RapidDocumentReference<T> documentReference, RapidCallback.Error errorCallback) {
		LiveData<RapidDocument<T>> liveData = new LiveData<RapidDocument<T>>() {
			private RapidDocumentSubscription<T> mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(document -> setValue(document)).onError(errorCallback);
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


	public static <T, S> LiveData<S> from(final RapidDocumentMapReference<T, S> documentReference) {
		return from(documentReference, Throwable::printStackTrace);
	}


	public static <T, S> LiveData<S> from(final RapidDocumentMapReference<T, S> documentReference, RapidCallback.Error errorCallback) {
		LiveData<S> liveData = new LiveData<S>() {
			private RapidDocumentSubscription<T> mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(document -> setValue(document)).onError(errorCallback);
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


	public static <T> LiveData<List<RapidDocument<T>>> from(final RapidCollectionReference<T> documentReference) {
		return from(documentReference, Throwable::printStackTrace);
	}


	public static <T> LiveData<List<RapidDocument<T>>> from(final RapidCollectionReference<T> documentReference, RapidCallback.Error errorCallback) {
		LiveData<List<RapidDocument<T>>> liveData = new LiveData<List<RapidDocument<T>>>() {
			private RapidCollectionSubscription mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(rapidDocuments -> setValue(rapidDocuments)).onError(errorCallback);
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


	public static <T, S> LiveData<List<S>> from(final RapidCollectionMapReference<T, S> documentReference) {
		return from(documentReference, Throwable::printStackTrace);
	}


	public static <T, S> LiveData<List<S>> from(final RapidCollectionMapReference<T, S> documentReference, RapidCallback.Error errorCallback) {
		LiveData<List<S>> liveData = new LiveData<List<S>>() {
			private RapidCollectionSubscription mSubscription;


			@Override
			protected void onActive() {
				mSubscription = documentReference.subscribe(rapidDocuments -> setValue(rapidDocuments)).onError(errorCallback);
			}


			@Override
			protected void onInactive() {
				mSubscription.unsubscribe();
			}
		};
		return liveData;
	}


}

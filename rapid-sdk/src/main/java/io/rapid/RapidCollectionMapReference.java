package io.rapid;


import java.util.ArrayList;
import java.util.List;


public class RapidCollectionMapReference<T, S> {

	private final MapFunction<T, S> mMapFunction;
	private final RapidCollectionReference<T> mCollectionReference;


	public interface MapFunction<T, S> {
		S map(RapidDocument<T> document);
	}


	public interface MapInnerFunction<T, S> {
		S map(T item);
	}


	public interface Callback<T> {
		void onValueChanged(List<T> items);
	}


	RapidCollectionMapReference(RapidCollectionReference<T> collectionReference, MapFunction<T, S> mapFunction) {
		mCollectionReference = collectionReference;
		mMapFunction = mapFunction;
	}


	public RapidCollectionSubscription subscribe(Callback<S> callback) {
		RapidCollectionSubscription<T> subscription = mCollectionReference.getSubscription();
		subscription.setCallback(rapidDocuments -> {
			List<S> result = new ArrayList<>();
			for(RapidDocument<T> rapidDocument : rapidDocuments) {
				result.add(mMapFunction.map(rapidDocument));
			}
			callback.onValueChanged(result);
		});
		mCollectionReference.getConnection().subscribe(subscription);

		return subscription;
	}


	public <U> RapidCollectionMapReference<T, U> map(MapInnerFunction<S, U> mapFunction) {
		return new RapidCollectionMapReference<>(mCollectionReference, document -> mapFunction.map(mMapFunction.map(document)));
	}
}

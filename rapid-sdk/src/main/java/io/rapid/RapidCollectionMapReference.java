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


	public interface MapCollectionCallback<T> {
		void onValueChanged(List<T> items);
	}


	public interface MapCollectionUpdatesCallback<T> {
		void onValueChanged(List<T> items, ListUpdate listUpdate);
	}


	RapidCollectionMapReference(RapidCollectionReference<T> collectionReference, MapFunction<T, S> mapFunction) {
		mCollectionReference = collectionReference;
		mMapFunction = mapFunction;
	}


	public RapidCollectionSubscription subscribe(MapCollectionCallback<S> callback) {
		return subscribeWithListUpdates((items, listUpdate) -> callback.onValueChanged(items));
	}


	public RapidCollectionSubscription subscribeWithListUpdates(MapCollectionUpdatesCallback<S> callback) {
		RapidCollectionSubscription<T> subscription = mCollectionReference.getSubscription();
		subscription.setCallback((rapidDocuments, listUpdate) -> {
			List<S> result = new ArrayList<>();
			for(RapidDocument<T> rapidDocument : rapidDocuments) {
				result.add(mMapFunction.map(rapidDocument));
			}
			callback.onValueChanged(result, listUpdate);
		});
		mCollectionReference.getConnection().subscribe(subscription);

		return subscription;
	}


	public <U> RapidCollectionMapReference<T, U> map(MapInnerFunction<S, U> mapFunction) {
		return new RapidCollectionMapReference<>(mCollectionReference, document -> mapFunction.map(mMapFunction.map(document)));
	}
}

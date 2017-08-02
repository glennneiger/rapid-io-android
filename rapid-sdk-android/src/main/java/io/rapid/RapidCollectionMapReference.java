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


	public interface MapCollectionUpdatesCallback<T> {
		void onValueChanged(List<T> items, ListUpdate listUpdate);
	}


	RapidCollectionMapReference(RapidCollectionReference<T> collectionReference, MapFunction<T, S> mapFunction) {
		mCollectionReference = collectionReference;
		mMapFunction = mapFunction;
	}


	public RapidCollectionSubscription subscribe(final RapidCallback.CollectionMapped<S> callback) {
		return subscribeWithListUpdates(new MapCollectionUpdatesCallback<S>() {
			@Override
			public void onValueChanged(List<S> items, ListUpdate listUpdate) {callback.onValueChanged(items);}
		});
	}


	public RapidCollectionSubscription subscribeWithListUpdates(final MapCollectionUpdatesCallback<S> callback) {
		RapidCollectionSubscription<T> subscription = mCollectionReference.getSubscription();
		subscription.setCallback(new RapidCallback.CollectionUpdates<T>() {
			@Override
			public void onValueChanged(List<RapidDocument<T>> rapidDocuments, ListUpdate listUpdate) {
				List<S> result = new ArrayList<>();
				for(RapidDocument<T> rapidDocument : rapidDocuments) {
					result.add(mMapFunction.map(rapidDocument));
				}
				callback.onValueChanged(result, listUpdate);
			}
		});
		mCollectionReference.getConnection().subscribe(subscription);

		return subscription;
	}


	public <U> RapidCollectionMapReference<T, U> map(final MapInnerFunction<S, U> mapFunction) {
		return new RapidCollectionMapReference<>(mCollectionReference, new MapFunction<T, U>() {
			@Override
			public U map(RapidDocument<T> document) {return mapFunction.map(mMapFunction.map(document));}
		});
	}
}

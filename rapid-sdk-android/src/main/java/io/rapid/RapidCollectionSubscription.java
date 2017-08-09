package io.rapid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import io.rapid.executor.RapidExecutor;
import io.rapid.utility.SortUtility;


public class RapidCollectionSubscription<T> extends BaseCollectionSubscription<T> {


	private List<RapidDocument<T>> mDocuments = new ArrayList<>();

	private Stack<Filter.Group> mFilterStack;
	private int mLimit = Config.DEFAULT_LIMIT;
	private int mSkip = 0;
	private EntityOrder mOrder;

	private RapidCallback.CollectionUpdates<T> mCallback;


	RapidCollectionSubscription(String collectionName, RapidExecutor uiThreadHandler) {
		super(collectionName, uiThreadHandler);
	}


	@Override
	synchronized int onDocumentUpdated(@NonNull RapidDocument<T> document) {

		ListUpdate listUpdate = null;
		document.setOrder(mOrder);
		int newDocumentPosition = -1;

		if(document.getBody() == null) {
			int pos = -1;
			for(int i = 0; i < mDocuments.size(); i++) {
				if(mDocuments.get(i).getId().equals(document.getId())) {
					pos = i;
					break;
				}
			}
			if(pos != -1) {
				mDocuments.remove(pos);
				listUpdate = new ListUpdate(ListUpdate.Type.REMOVED, pos, ListUpdate.NO_POSITION);
			}
		} else {
			int documentPosition = -1;
			for(int i = 0; i < mDocuments.size(); i++) {
				if(mDocuments.get(i).getId().equals(document.getId())) {
					documentPosition = i;
				}
			}
			if(documentPosition != -1) mDocuments.remove(documentPosition);
			newDocumentPosition = SortUtility.getInsertPosition(mDocuments, document);

			if(documentPosition != -1) {
				listUpdate = new ListUpdate(documentPosition == newDocumentPosition ? ListUpdate.Type.UPDATED : ListUpdate.Type.MOVED, documentPosition, newDocumentPosition);
				mDocuments.add(newDocumentPosition, document);
			} else {
				listUpdate = new ListUpdate(ListUpdate.Type.ADDED, ListUpdate.NO_POSITION, newDocumentPosition);
				mDocuments.add(newDocumentPosition, document);
			}
		}
		invokeChange(listUpdate);
		return newDocumentPosition;
	}


	@Override
	EntityOrder getOrder() {
		return mOrder;
	}


	@NonNull
	@Override
	public RapidCollectionSubscription<T> onError(RapidCallback.Error callback) {
		return (RapidCollectionSubscription<T>) super.onError(callback);
	}


	@Override
	int getSkip() {
		return mSkip;
	}


	void setSkip(int skip) {
		mSkip = skip;
		invalidateFingerprintCache();
	}


	@Override
	List<RapidDocument<T>> getDocuments() {
		return mDocuments;
	}


	@Override
	int getLimit() {
		return mLimit;
	}


	void setLimit(int limit) {
		mLimit = limit;
		invalidateFingerprintCache();
	}


	@Nullable
	@Override
	Filter getFilter() {
		if(mFilterStack == null) return null;

		if(getFilterStack().size() != 1) {
			throw new IllegalArgumentException("Wrong filter structure");
		}
		return removeRedundantGroups(mFilterStack.peek());
	}


	void orderBy(String property, Sorting sorting) {
		if(mOrder == null) mOrder = new EntityOrder();
		mOrder.putOrder(property, sorting);
		invalidateFingerprintCache();
	}


	synchronized void setDocuments(List<RapidDocument<T>> rapidDocuments, DataState dataState) {
		mDataState = dataState;
		mDocuments = rapidDocuments;
		for(RapidDocument<T> doc : mDocuments) {
			doc.setOrder(mOrder);
		}
		invokeChange(new ListUpdate(dataState == DataState.LOADED_FROM_DISK_CACHE || dataState == DataState.LOADED_FROM_MEMORY_CACHE ? ListUpdate.Type.NEW_LIST_FROM_CACHE : ListUpdate.Type.NEW_LIST, ListUpdate.NO_POSITION, ListUpdate.NO_POSITION));
	}


	void setCallback(RapidCallback.CollectionUpdates<T> callback) {
		mCallback = callback;
	}


	Stack<Filter.Group> getFilterStack() {
		if(mFilterStack == null) {
			mFilterStack = new Stack<>();
			mFilterStack.push(new Filter.And());
		}
		invalidateFingerprintCache();
		return mFilterStack;
	}


	private Filter removeRedundantGroups(@NonNull Filter.Group rootFilter) {

		// remove root redundant and/or
		// TODO: do this inside Filter tree as well
		// TODO: remove unnecessary nested ANDs/ORs
		if(rootFilter.filters.size() == 0) {
			return null;
		} else if(rootFilter.filters.size() == 1) {
			return new Filter.Single(rootFilter.filters.get(0));
		} else {
			return rootFilter;
		}
	}


	private synchronized void invokeChange(ListUpdate listUpdate) {
		mExecutor.doOnMain(() -> {
			synchronized(mCallback) {
				mCallback.onValueChanged(new ArrayList<>(mDocuments), listUpdate);
			}
		});
	}
}
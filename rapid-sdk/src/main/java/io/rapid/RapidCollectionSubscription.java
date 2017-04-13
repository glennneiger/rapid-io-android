package io.rapid;


import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class RapidCollectionSubscription<T> extends Subscription<T> {


	private List<RapidDocument<T>> mDocuments = new ArrayList<>();

	private Stack<Filter.Group> mFilterStack = new Stack<>();
	private int mLimit = Config.DEFAULT_LIMIT;
	private int mSkip = 0;
	private EntityOrder mOrder;

	private RapidCallback.CollectionUpdates<T> mCallback;


	RapidCollectionSubscription(String collectionName, Handler uiThreadHandler) {
		super(collectionName, uiThreadHandler);
	}


	@Override
	synchronized void onDocumentUpdated(String previousSiblingId, RapidDocument<T> document) {

		ListUpdate listUpdate = null;

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
			int previousSiblingPosition = -1;
			int documentPosition = -1;
			for(int i = 0; i < mDocuments.size(); i++) {
				if(mDocuments.get(i).getId().equals(previousSiblingId)) {
					previousSiblingPosition = i;
				} else if(mDocuments.get(i).getId().equals(document.getId())) {
					documentPosition = i;
				}
			}

			if(documentPosition != -1) {
				mDocuments.remove(documentPosition);
				listUpdate = new ListUpdate(documentPosition == previousSiblingPosition + 1 ? ListUpdate.Type.UPDATED : ListUpdate.Type.MOVED, documentPosition, previousSiblingPosition + 1);
			} else {
				listUpdate = new ListUpdate(ListUpdate.Type.ADDED, ListUpdate.NO_POSITION, previousSiblingPosition + 1);
			}
			mDocuments.add(previousSiblingPosition + 1, document);
		}
		invokeChange(listUpdate);
	}


	@Override
	EntityOrder getOrder() {
		return mOrder;
	}


	@Override
	public RapidCollectionSubscription onError(RapidCallback.Error callback) {
		mErrorCallback = callback;
		return this;
	}


	@Override
	int getSkip() {
		return mSkip;
	}


	public List<RapidDocument<T>> getDocuments() {
		return mDocuments;
	}


	void setSkip(int skip) {
		mSkip = skip;
		invalidateFingerprintCache();
	}


	@Override
	int getLimit() {
		return mLimit;
	}


	void setLimit(int limit) {
		mLimit = limit;
		invalidateFingerprintCache();
	}


	@Override
	Filter getFilter() {
		if(getFilterStack().size() != 1) {
			throw new IllegalArgumentException("Wrong filter structure");
		}
		return mFilterStack.peek();
	}


	void orderBy(String property, Sorting sorting) {
		if(mOrder == null) mOrder = new EntityOrder();
		mOrder.putOrder(property, sorting);
		invalidateFingerprintCache();
	}


	synchronized void setDocuments(List<RapidDocument<T>> rapidDocuments, boolean fromCache) {
		mDocuments = rapidDocuments;
		invokeChange(new ListUpdate(fromCache ? ListUpdate.Type.NEW_LIST_FROM_CACHE : ListUpdate.Type.NEW_LIST, ListUpdate.NO_POSITION, ListUpdate.NO_POSITION));
	}


	void setCallback(RapidCallback.CollectionUpdates<T> callback) {
		mCallback = callback;
	}


	Stack<Filter.Group> getFilterStack() {
		invalidateFingerprintCache();
		return mFilterStack;
	}


	private synchronized void invokeChange(ListUpdate listUpdate) {
		mUiThreadHandler.post(() -> {
			synchronized(mCallback) {
				mCallback.onValueChanged(mDocuments, listUpdate);
			}
		});
	}
}
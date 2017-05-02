package io.rapid;


import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class RapidCollectionSubscription<T> extends Subscription<T> {


	private List<RapidDocument<T>> mDocuments = new ArrayList<>();

	private Stack<Filter.Group> mFilterStack;
	private int mLimit = Config.DEFAULT_LIMIT;
	private int mSkip = 0;
	private EntityOrder mOrder;

	private RapidCallback.CollectionUpdates<T> mCallback;


	RapidCollectionSubscription(String collectionName, Handler uiThreadHandler) {
		super(collectionName, uiThreadHandler);
	}


	@Override
	synchronized void onDocumentUpdated(RapidDocument<T> document) {

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
			int documentPosition = -1;
			for(int i = 0; i < mDocuments.size(); i++) {
				if(mDocuments.get(i).getId().equals(document.getId())) {
					documentPosition = i;
				}
			}
			if(documentPosition != -1) mDocuments.remove(documentPosition);
			int newDocumentPosition = getDocumentPosition(document, 0, mDocuments.size());

			if(documentPosition != -1) {
				listUpdate = new ListUpdate(documentPosition == newDocumentPosition ? ListUpdate.Type.UPDATED : ListUpdate.Type.MOVED, documentPosition, newDocumentPosition);
				mDocuments.add(newDocumentPosition, document);
			} else {
				listUpdate = new ListUpdate(ListUpdate.Type.ADDED, ListUpdate.NO_POSITION, newDocumentPosition);
                mDocuments.add(newDocumentPosition, document);
			}
		}
		invokeChange(listUpdate);
	}


	private int getDocumentPosition(RapidDocument<T> newDocument, int leftIndex, int rightIndex)
	{
		if(leftIndex == rightIndex) return leftIndex;

		int middleIndex = leftIndex + (rightIndex-leftIndex) / 2;
		if(compareDocuments(newDocument, mDocuments.get(middleIndex)) > 0)
		{
			return getDocumentPosition(newDocument, middleIndex + 1, rightIndex);
		}
		else
		{
			return getDocumentPosition(newDocument, leftIndex, Math.max(leftIndex, middleIndex - 1));
		}
	}


	private int compareDocuments(RapidDocument<T> newDocument, RapidDocument<T> oldDocument)
	{
		int depth = 0;
		while(newDocument.getSorting().get(depth).compareTo(oldDocument.getSorting().get(depth)) == 0)
		{
			depth++;

			if(depth == newDocument.getSorting().size())
			{
				depth--;
				break;
			}
		}

		Sorting sortingType;
		if(depth == newDocument.getSorting().size() - 1)
			sortingType = Sorting.ASC;
		else
			sortingType = mOrder.getOrderList().get(depth).getSorting();

		if(sortingType == Sorting.ASC)
			return newDocument.getSorting().get(depth).compareTo(oldDocument.getSorting().get(depth));
		else
			return -newDocument.getSorting().get(depth).compareTo(oldDocument.getSorting().get(depth));
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


	@Override
	List<RapidDocument<T>> getDocuments() {
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
		if(mFilterStack == null) return null;

		if(getFilterStack().size() != 1) {
			throw new IllegalArgumentException("Wrong filter structure");
		}
		return removeRedundantGroups(mFilterStack.peek());
	}


	private Filter removeRedundantGroups(Filter.Group rootFilter) {

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
		if(mFilterStack == null) {
			mFilterStack = new Stack<>();
			mFilterStack.push(new Filter.And());
		}
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
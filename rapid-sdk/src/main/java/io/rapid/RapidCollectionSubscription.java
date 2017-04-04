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

	private RapidCollectionCallback<T> mCallback;


	RapidCollectionSubscription(String collectionName, Handler uiThreadHandler) {
		super(collectionName, uiThreadHandler);
	}


	@Override
	void onDocumentUpdated(RapidDocument<T> document) {
		if (document.getBody()==null){
			for(RapidDocument<T> doc : mDocuments) {
				if (doc.getId().equals(document.getId())){
					mDocuments.remove(doc);
					break;
				}
			}
		} else {
			boolean modified = false;
			for(int i = 0; i < mDocuments.size(); i++) {
				if(mDocuments.get(i).getId().equals(document.getId())) {
					mDocuments.set(i, document);
					modified = true;
					break;
				}
			}
			if(!modified) {
				mDocuments.add(document);
			}
		}
		invokeChange();
	}


	void orderBy(String property, Sorting sorting) {
		if(mOrder == null) mOrder = new EntityOrder();
		mOrder.putOrder(property, sorting);
	}


	void setDocuments(List<RapidDocument<T>> rapidDocuments) {
		mDocuments = rapidDocuments;
		invokeChange();
	}


	void setCallback(RapidCollectionCallback<T> callback) {
		mCallback = callback;
	}


	@Override
	EntityOrder getOrder() {
		return mOrder;
	}


	@Override
	int getSkip() {
		return mSkip;
	}


	void setSkip(int skip) {
		mSkip = skip;
	}


	@Override
	int getLimit() {
		return mLimit;
	}


	void setLimit(int limit) {
		mLimit = limit;
	}


	@Override
	Filter getFilter() {
		if(getFilterStack().size() != 1) {
			throw new IllegalArgumentException("Wrong filter structure");
		}
		return mFilterStack.peek();
	}


	Stack<Filter.Group> getFilterStack() {
		return mFilterStack;
	}


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}


	private void invokeChange() {
		mUiThreadHandler.post(() -> mCallback.onValueChanged(mDocuments));
	}
}
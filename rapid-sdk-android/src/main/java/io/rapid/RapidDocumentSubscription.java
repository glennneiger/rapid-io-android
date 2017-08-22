package io.rapid;


import java.util.ArrayList;
import java.util.List;

import io.rapid.executor.RapidExecutor;


public class RapidDocumentSubscription<T> extends BaseCollectionSubscription<T> {

	private RapidCallback.Document<T> mCallback;
	private String mId;
	private RapidDocument<T> mDocument;


	RapidDocumentSubscription(String documentId, String collectionName, RapidExecutor executor) {
		super(collectionName, executor);
		mId = documentId;
	}


	@Override
	int onDocumentUpdated(RapidDocument<T> document) {
		mDocument = document;
		invokeChange();
		return 0;
	}


	@Override
	public RapidDocumentSubscription<T> onError(RapidCallback.Error callback) {
		return (RapidDocumentSubscription<T>) super.onError(callback);
	}


	@Override
	int getSkip() {
		return 0;
	}


	@Override
	int getLimit() {
		return 1;
	}


	@Override
	Filter getFilter() {
		return new FilterValue(RapidCollectionReference.PROPERTY_ID, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, mId));
	}


	@Override
	EntityOrder getOrder() {
		return null;
	}


	@Override
	List<RapidDocument<T>> getDocuments() {
		List<RapidDocument<T>> list = new ArrayList<>();
		list.add(mDocument);
		return list;
	}


	public String getId() {
		return mId;
	}


	public RapidDocument<T> getDocument() {
		return mDocument;
	}


	void setDocument(RapidDocument<T> rapidDocument, DataState dataState) {
		mDataState = dataState;
		mDocument = rapidDocument;
		invokeChange();
	}


	void setCallback(RapidCallback.Document<T> callback) {
		mCallback = callback;
	}


	private void invokeChange() {
		mExecutor.doOnMain(() -> mCallback.onValueChanged(mDocument));
	}
}

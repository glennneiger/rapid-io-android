package io.rapid;


import android.os.Handler;

import java.util.ArrayList;
import java.util.List;


public class RapidDocumentSubscription<T> extends Subscription<T> {

	private RapidCallback.Document<T> mCallback;
	private String mId;
	private RapidDocument<T> mDocument;


	RapidDocumentSubscription(String documentId, String collectionName, Handler uiThreadHandler) {
		super(collectionName, uiThreadHandler);
		mId = documentId;
	}


	@Override
	int onDocumentUpdated(RapidDocument<T> document) {
		mDocument = document;
		invokeChange();
		return 0;
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
		return new FilterValue(Config.ID_IDENTIFIER, new FilterValue.StringPropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, mId));
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


	@Override
	public RapidDocumentSubscription onError(RapidCallback.Error callback) {
		mErrorCallback = callback;
		return this;
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
		mInitialValue = true;
	}


	void setCallback(RapidCallback.Document<T> callback) {
		mCallback = callback;
	}


	private void invokeChange() {
		mUiThreadHandler.post(() -> mCallback.onValueChanged(mDocument));
	}
}

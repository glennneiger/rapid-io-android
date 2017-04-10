package io.rapid;


import android.os.Handler;


public class RapidDocumentSubscription<T> extends Subscription<T> {

	private RapidCallback.Document<T> mCallback;
	private String mId;
	private RapidDocument<T> mDocument;


	RapidDocumentSubscription(String documentId, String collectionName, Handler uiThreadHandler) {
		super(collectionName, uiThreadHandler);
		mId = documentId;
	}


	@Override
	void onDocumentUpdated(String previousSiblingId, RapidDocument<T> document) {
		mDocument = document;
		invokeChange();
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


	public String getId() {
		return mId;
	}


	void setDocument(RapidDocument<T> rapidDocument) {
		mDocument = rapidDocument;
		invokeChange();
	}


	void setCallback(RapidCallback.Document<T> callback) {
		mCallback = callback;
	}


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}


	private void invokeChange() {
		mUiThreadHandler.post(() -> mCallback.onValueChanged(mDocument));
	}
}

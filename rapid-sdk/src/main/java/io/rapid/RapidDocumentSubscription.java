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


	@Override
	public RapidDocumentSubscription onError(RapidCallback.Error callback)
	{
		mErrorCallback = callback;
		return this;
	}


	public String getId() {
		return mId;
	}


	public RapidDocument<T> getDocument() {
		return mDocument;
	}


	void setDocument(RapidDocument<T> rapidDocument) {
		mDocument = rapidDocument;
		invokeChange();
	}


	void setCallback(RapidCallback.Document<T> callback) {
		mCallback = callback;
	}



	private void invokeChange() {
		mUiThreadHandler.post(() -> mCallback.onValueChanged(mDocument));
	}
}

package io.rapid;


import android.os.Handler;


public class RapidDocumentSubscription<T> extends Subscription<T> {

	private RapidDocumentCallback<T> mCallback;
	private String mId;
	private RapidDocument<T> mDocument;


	RapidDocumentSubscription(String documentId, String collectionName, Handler uiThreadHandler) {
		super(collectionName, uiThreadHandler);
		mId = documentId;
	}


	@Override
	void onDocumentUpdated(RapidDocument<T> document) {
		mDocument = document;
		invokeChange();
	}


	@Override
	MessageSub createSubscriptionMessage(String subscriptionId) {
		MessageSub subscriptionMsg = new MessageSub(IdProvider.getNewEventId(), mCollectionName, subscriptionId);
		subscriptionMsg.setSkip(0);
		subscriptionMsg.setLimit(1);
		subscriptionMsg.setFilter(new FilterValue(Config.ID_IDENTIFIER, new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, mId)));
		return subscriptionMsg;
	}


	public String getId() {
		return mId;
	}


	void setDocument(RapidDocument<T> rapidDocument) {
		mDocument = rapidDocument;
		invokeChange();
	}


	void setCallback(RapidDocumentCallback<T> callback) {
		mCallback = callback;
	}


	void setOnUnsubscribeCallback(OnUnsubscribeCallback callback) {
		mOnUnsubscribeCallback = callback;
	}


	private void invokeChange() {
		mUiThreadHandler.post(() -> mCallback.onValueChanged(mDocument));
	}
}

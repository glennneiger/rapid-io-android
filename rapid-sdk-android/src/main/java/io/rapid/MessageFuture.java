package io.rapid;

import io.rapid.executor.RapidExecutor;


class MessageFuture {
	private Message mMessage;
	private RapidFuture mRapidFuture;
	private RapidExecutor mExecutor;
	private long mSentTimestamp;
	private String mMessageJson;


	MessageFuture(Message message, String messageJson, RapidFuture rapidFuture, RapidExecutor executor) {
		mMessage = message;
		mMessageJson = messageJson;
		mRapidFuture = rapidFuture;
		mExecutor = executor;
		mSentTimestamp = System.currentTimeMillis();
	}


	public Message getMessage() {
		return mMessage;
	}


	public RapidExecutor getExecutor() {
		return mExecutor;
	}


	RapidFuture getRapidFuture() {
		return mRapidFuture;
	}


	long getSentTimestamp() {
		return mSentTimestamp;
	}


	String getMessageJson() {
		return mMessageJson;
	}
}
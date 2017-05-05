package io.rapid;

class MessageFuture {
	private Message mMessage;
	private RapidFuture mRapidFuture;
	private long mSentTimestamp;
	private String mMessageJson;


	MessageFuture(Message message, String messageJson, RapidFuture rapidFuture) {
		mMessage = message;
		mMessageJson = messageJson;
		mRapidFuture = rapidFuture;
		mSentTimestamp = System.currentTimeMillis();
	}


	public Message getMessage() {
		return mMessage;
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
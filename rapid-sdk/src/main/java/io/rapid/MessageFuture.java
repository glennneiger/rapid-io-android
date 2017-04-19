package io.rapid;

class MessageFuture {
	Message mMessage;
	RapidFuture mRapidFuture;
	long mSentTimestamp;
	String mMessageJson;


	public MessageFuture(Message message, String messageJson, RapidFuture rapidFuture) {
		mMessage = message;
		mMessageJson = messageJson;
		mRapidFuture = rapidFuture;
		mSentTimestamp = System.currentTimeMillis();
	}


	public Message getMessage() {
		return mMessage;
	}


	public RapidFuture getRapidFuture() {
		return mRapidFuture;
	}


	public long getSentTimestamp() {
		return mSentTimestamp;
	}


	public String getMessageJson() {
		return mMessageJson;
	}
}
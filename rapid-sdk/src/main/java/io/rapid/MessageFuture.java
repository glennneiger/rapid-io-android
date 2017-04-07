package io.rapid;

class MessageFuture
{
	Message mMessage;
	RapidFuture mRapidFuture;
	long mSentTimestamp;


	public MessageFuture(Message message, RapidFuture rapidFuture)
	{
		mMessage = message;
		mRapidFuture = rapidFuture;
		mSentTimestamp = System.currentTimeMillis();
	}


	public Message getMessage()
	{
		return mMessage;
	}


	public RapidFuture getRapidFuture()
	{
		return mRapidFuture;
	}


	public long getSentTimestamp()
	{
		return mSentTimestamp;
	}
}
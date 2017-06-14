package io.rapid;


public class RapidMessage<T> {
	private String mChannelName;
	private T mBody;


	RapidMessage(String channelName, T body) {
		mChannelName = channelName;
		mBody = body;
	}


	public String getChannelName() {
		return mChannelName;
	}


	public T getBody() {
		return mBody;
	}
}

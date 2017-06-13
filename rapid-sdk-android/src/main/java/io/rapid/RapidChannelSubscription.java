package io.rapid;


import android.os.Handler;


public class RapidChannelSubscription<T> extends BaseSubscription{
	private String mChannelName;
	private RapidCallback.Message<T> mCallback;


	RapidChannelSubscription(String channelName, Handler uiThreadHandler) {
		super(uiThreadHandler);
		mChannelName = channelName;
	}

	public void setCallback(RapidCallback.Message<T> callback) {
		mCallback = callback;
	}


	String getChannelName() {
		return mChannelName;
	}


	@Override
	public RapidChannelSubscription onError(RapidCallback.Error callback) {
		return (RapidChannelSubscription) super.onError(callback);
	}


	void onMessage(T message) {
		mCallback.onMessageReceived(message);
	}
}

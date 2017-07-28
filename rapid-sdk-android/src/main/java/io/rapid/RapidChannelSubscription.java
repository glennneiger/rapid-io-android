package io.rapid;


import io.rapid.executor.RapidExecutor;


public class RapidChannelSubscription<T> extends Subscription {
	private String mChannelName;
	private RapidCallback.Message<T> mCallback;


	RapidChannelSubscription(String channelName, RapidExecutor uiThreadHandler) {
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
	public RapidChannelSubscription<T> onError(RapidCallback.Error callback) {
		return (RapidChannelSubscription<T>) super.onError(callback);
	}


	void onMessage(RapidMessage<T> message) {
		mCallback.onMessageReceived(message);
	}
}

package io.rapid;


import android.support.annotation.NonNull;

import io.rapid.executor.RapidExecutor;


public class RapidChannelSubscription<T> extends Subscription {
	private String mChannelName;
	private RapidCallback.Message<T> mCallback;


	RapidChannelSubscription(String channelName, RapidExecutor uiThreadHandler) {
		super(uiThreadHandler);
		mChannelName = channelName;
	}


	@NonNull
	@Override
	public RapidChannelSubscription<T> onError(RapidCallback.Error callback) {
		return (RapidChannelSubscription<T>) super.onError(callback);
	}


	public void setCallback(RapidCallback.Message<T> callback) {
		mCallback = callback;
	}


	String getChannelName() {
		return mChannelName;
	}


	void onMessage(RapidMessage<T> message) {
		mCallback.onMessageReceived(message);
	}
}

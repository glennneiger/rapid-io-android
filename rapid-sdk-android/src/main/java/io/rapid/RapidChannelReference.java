package io.rapid;


import android.os.Handler;


public class RapidChannelReference<T> {


	private ChannelConnection<T> mChannelConnection;
	private String mChannelName;
	private Handler mOriginalThreadHandler;


	RapidChannelReference(ChannelConnection<T> channelConnection, String channelName, Handler originalThreadHandler) {
		mChannelConnection = channelConnection;
		mChannelName = channelName;
		mOriginalThreadHandler = originalThreadHandler;
	}


	public RapidChannelSubscription<T> subscribe(RapidCallback.Message<T> callback) {
		RapidChannelSubscription<T> subscription = new RapidChannelSubscription<>(mChannelName, mOriginalThreadHandler);
		subscription.setSubscriptionId(IdProvider.getNewSubscriptionId());
		subscription.setCallback(callback);
		mChannelConnection.subscribe(subscription);
		return subscription;
	}


	public RapidFuture publish(T message) {
		return mChannelConnection.publish(message);
	}
}

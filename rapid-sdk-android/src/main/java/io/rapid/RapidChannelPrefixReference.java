package io.rapid;


import android.os.Handler;


public class RapidChannelPrefixReference<T> {


	protected ChannelConnection<T> mChannelConnection;
	private String mChannelName;
	private Handler mOriginalThreadHandler;


	RapidChannelPrefixReference(ChannelConnection<T> channelConnection, String channelName, Handler originalThreadHandler) {
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





	public String getChannelName() {
		return mChannelName;
	}
}

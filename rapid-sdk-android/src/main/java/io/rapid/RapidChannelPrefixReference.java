package io.rapid;


import io.rapid.executor.RapidExecutor;


public class RapidChannelPrefixReference<T> {


	protected ChannelConnection<T> mChannelConnection;
	private String mChannelName;
	private RapidExecutor mExecutor;


	RapidChannelPrefixReference(ChannelConnection<T> channelConnection, String channelName, RapidExecutor executor) {
		mChannelConnection = channelConnection;
		mChannelName = channelName;
		mExecutor = executor;
	}


	public RapidChannelSubscription<T> subscribe(RapidCallback.Message<T> callback) {
		RapidChannelSubscription<T> subscription = new RapidChannelSubscription<>(mChannelName, mExecutor);
		subscription.setSubscriptionId(IdProvider.getNewSubscriptionId());
		subscription.setCallback(callback);
		mChannelConnection.subscribe(subscription);
		return subscription;
	}





	public String getChannelName() {
		return mChannelName;
	}
}

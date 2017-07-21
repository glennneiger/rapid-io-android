package io.rapid;


interface ChannelConnection<T> {
	void subscribe(RapidChannelSubscription<T> subscription);
	RapidFuture publish(T message);
	void onMessage(String subscriptionId, String channelName, String body);
	void onError(String subscriptionId, RapidError error);
	boolean hasSubscription(String subscriptionId);
}

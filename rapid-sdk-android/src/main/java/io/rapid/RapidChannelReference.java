package io.rapid;


import io.rapid.executor.RapidExecutor;


public class RapidChannelReference<T> extends RapidChannelPrefixReference<T> {
	RapidChannelReference(ChannelConnection<T> channelConnection, String channelName, RapidExecutor originalThreadHandler) {
		super(channelConnection, channelName, originalThreadHandler);
	}


	public RapidFuture publish(T message) {
		return mChannelConnection.publish(message);
	}
}

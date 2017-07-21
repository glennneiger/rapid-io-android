package io.rapid;


public class RapidChannelReference<T> extends RapidChannelPrefixReference<T> {
	RapidChannelReference(ChannelConnection<T> channelConnection, String channelName, RapidExecutor executor) {
		super(channelConnection, channelName, executor);
	}


	public RapidFuture publish(T message) {
		return mChannelConnection.publish(message);
	}
}

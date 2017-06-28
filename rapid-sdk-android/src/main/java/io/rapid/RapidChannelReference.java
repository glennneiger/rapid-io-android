package io.rapid;


import android.os.Handler;


public class RapidChannelReference<T> extends RapidChannelPrefixReference<T> {
	RapidChannelReference(ChannelConnection<T> channelConnection, String channelName, Handler originalThreadHandler) {
		super(channelConnection, channelName, originalThreadHandler);
	}


	public RapidFuture publish(T message) {
		return mChannelConnection.publish(message);
	}
}

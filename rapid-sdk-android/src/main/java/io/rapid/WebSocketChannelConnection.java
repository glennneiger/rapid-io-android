package io.rapid;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


class WebSocketChannelConnection<T> implements ChannelConnection<T> {
	private RapidConnection mConnection;
	private JsonConverterProvider mJsonConverter;
	private String mChannelName;
	private final Class<T> mMessageClass;
	private final RapidLogger mLogger;
	private boolean mNameIsPrefix;
	private Map<String, RapidChannelSubscription<T>> mSubscriptions = new HashMap<>();


	WebSocketChannelConnection(RapidConnection connection, JsonConverterProvider jsonConverter, String channelName, Class<T> messageClass, RapidLogger debugLogger, boolean nameIsPrefix) {
		mConnection = connection;
		mJsonConverter = jsonConverter;
		mChannelName = channelName;
		mMessageClass = messageClass;
		mLogger = debugLogger;
		mNameIsPrefix = nameIsPrefix;
	}


	@Override
	public void subscribe(final RapidChannelSubscription<T> subscription) {
		subscription.setOnUnsubscribeCallback(new BaseCollectionSubscription.OnUnsubscribeCallback() {
			@Override
			public void onUnsubscribe() {WebSocketChannelConnection.this.onSubscriptionUnsubscribed(subscription);}
		});
		subscription.setSubscribed(true);
		mSubscriptions.put(subscription.getSubscriptionId(), subscription);
		mConnection.subscribeChannel(subscription.getSubscriptionId(), subscription, mNameIsPrefix);
	}


	private void onSubscriptionUnsubscribed(RapidChannelSubscription subscription) {
		mLogger.logI("Unsubscribing from channel '%s'", mChannelName);

		mSubscriptions.remove(subscription.getSubscriptionId());
		mConnection.onUnsubscribe(subscription);
	}


	@Override
	public RapidFuture publish(final T message) {
		return mConnection.publish(mChannelName, new FutureResolver<String>() {
			@Override
			public String resolve() {
				String messageJson;
				try {
					messageJson = mJsonConverter.get().toJson(message);
				} catch(IOException e) {
					throw new IllegalArgumentException(e);
				}
				mLogger.logI("Publishing message to channel %s. Message:", mChannelName);
				mLogger.logJson(messageJson);
				return messageJson;
			}
		});
	}


	@Override
	public void onMessage(String subscriptionId, String channelName, String messageBody) {

		mLogger.logI("New message in channel '%s'", mChannelName);
		mLogger.logJson(messageBody);

		RapidChannelSubscription<T> subscription = mSubscriptions.get(subscriptionId);
		T message = parseMessage(messageBody);
		subscription.onMessage(new RapidMessage<>(channelName, message));
	}


	@Override
	public void onError(String subscriptionId, RapidError error) {
		RapidChannelSubscription<T> subscription = mSubscriptions.get(subscriptionId);
		if(subscription != null) {
			subscription.invokeError(error);
			mSubscriptions.remove(subscription.getSubscriptionId());
			subscription.setSubscribed(false);
		}
	}


	@Override
	public boolean hasSubscription(String subscriptionId) {
		return mSubscriptions.containsKey(subscriptionId);
	}


	private T parseMessage(String messageBody) {
		try {
			return mJsonConverter.get().fromJson(messageBody, mMessageClass);
		} catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
}

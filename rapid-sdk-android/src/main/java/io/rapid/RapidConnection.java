package io.rapid;


abstract class RapidConnection {

	Callback mCallback;


	interface Callback {
		void onValue(String subscriptionId, String collectionId, String documentsJson);
		void onFetchResult(String fetchId, String collectionId, String documentsJson);
		void onUpdate(String subscriptionId, String collectionId, String documentJson);
		void onCollectionError(String subscriptionId, String collectionId, RapidError error);
		void onChannelError(String subscriptionId, String channelId, RapidError error);
		void onRemove(String subscriptionId, String collectionId, String documentJson);
		void onTimedOut();
		void onReconnected();
		void onChannelMessage(String subscriptionId, String channelName, String body);
	}


	RapidConnection(Callback callback) {
		mCallback = callback;
	}


	abstract RapidFuture authorize(String token);


	abstract RapidFuture deauthorize();


	abstract boolean isAuthenticated();


	abstract void addConnectionStateListener(RapidConnectionStateListener listener);


	abstract void removeConnectionStateListener(RapidConnectionStateListener listener);


	abstract void removeAllConnectionStateListeners();


	abstract ConnectionState getConnectionState();


	abstract void subscribe(BaseCollectionSubscription subscription);


	public abstract void subscribeChannel(String subscriptionId, RapidChannelSubscription subscription, boolean nameIsPrefix);


	abstract void fetch(String fetchId, BaseCollectionSubscription subscription);


	abstract void onUnsubscribe(BaseCollectionSubscription subscription);

	abstract void onUnsubscribe(RapidChannelSubscription subscription);


	public abstract RapidFuture mutate(String collectionName, FutureResolver<String> documentJson);


	public abstract RapidFuture merge(String collectionName, FutureResolver<String> documentJson);


	public abstract RapidFuture publish(String channelName, FutureResolver<String> messageJson);


	public abstract RapidFuture delete(String collectionName, FutureResolver<String> documentJson);


	public abstract void setConnectionTimeout(long connectionTimeoutMs);

	public abstract RapidFuture getSetverTimeOffset(RapidCallback.TimeOffset callback);


	Callback getCallback() {
		return mCallback;
	}
}

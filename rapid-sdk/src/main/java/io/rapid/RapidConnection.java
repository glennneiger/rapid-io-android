package io.rapid;


interface RapidConnection {

	interface Listener {
		void onMessage(MessageBase messageBase);
		void onReconnected();
	}

	MessageFuture sendMessage(MessageBase message);
	void addConnectionStateListener(RapidConnectionStateListener listener);
	void removeConnectionStateListener(RapidConnectionStateListener listener);
	void removeAllConnectionStateListeners();
	ConnectionState getConnectionState();
	void onSubscribe();
	void onUnsubscribe(boolean lastSubscription);
}

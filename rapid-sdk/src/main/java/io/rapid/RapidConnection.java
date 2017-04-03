package io.rapid;


abstract class RapidConnection {

	Callback mCallback;


	interface Callback {
		void onMessage(MessageBase messageBase);
		void onReconnected();
	}


	public RapidConnection(Callback callback) {
		mCallback = callback;
	}


	abstract MessageFuture sendMessage(MessageBase message);


	abstract void addConnectionStateListener(RapidConnectionStateListener listener);


	abstract void removeConnectionStateListener(RapidConnectionStateListener listener);


	abstract void removeAllConnectionStateListeners();


	abstract ConnectionState getConnectionState();


	abstract void onSubscribe();


	abstract void onUnsubscribe(boolean lastSubscription);


	Callback getCallback() {
		return mCallback;
	}
}

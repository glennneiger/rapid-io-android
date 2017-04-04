package io.rapid;


abstract class RapidConnection {

	Callback mCallback;


	interface Callback {
		void onValue(String subscriptionId, String collectionId, String documentsJson);
		void onUpdate(String subscriptionId, String collectionId, String documentJson);
		void onReconnected();
	}


	public RapidConnection(Callback callback) {
		mCallback = callback;
	}


	abstract void addConnectionStateListener(RapidConnectionStateListener listener);


	abstract void removeConnectionStateListener(RapidConnectionStateListener listener);


	abstract void removeAllConnectionStateListeners();


	abstract ConnectionState getConnectionState();


	abstract void subscribe(String subscriptionId, Subscription subscription);


	abstract void onUnsubscribe(Subscription subscription);


	public abstract RapidFuture mutate(String collectionName, String documentJson);


	Callback getCallback() {
		return mCallback;
	}
}

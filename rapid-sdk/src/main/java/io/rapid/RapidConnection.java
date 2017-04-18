package io.rapid;


abstract class RapidConnection {

	Callback mCallback;


	interface Callback {
		void onValue(String subscriptionId, String collectionId, String documentsJson);
		void onUpdate(String subscriptionId, String collectionId, String previousSiblingId, String documentJson);
		void onError(String subscriptionId, String collectionId, RapidError error);
		void onTimedOut();
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


	public abstract RapidFuture mutate(String collectionName, FutureResolver<String> documentJson);


	Callback getCallback() {
		return mCallback;
	}
}

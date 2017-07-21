package io.rapid;

import org.json.JSONException;


abstract class WebSocketConnection {
	String mServerURI;
	protected RapidExecutor mExecutor;
	WebSocketConnectionListener mListener;


	interface WebSocketConnectionListener {
		void onOpen();
		void onMessage(Message message);
		void onClose(CloseReason reason);
//		void onCollectionError(Exception ex);
	}


	WebSocketConnection(String serverURI, RapidExecutor executor, WebSocketConnectionListener listener) {
		mServerURI = serverURI;
		mExecutor = executor;
		mListener = listener;
	}


	abstract void connectToServer();
	abstract void sendMessage(String message);


	void disconnectFromServer(boolean sendDisconnectMessage) {
		if(sendDisconnectMessage) {
			try {
				sendMessage(new Message.Dis().toJson().toString());
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}


	void handleNewMessage(Message parsedMessage) {
		if(mListener != null) mListener.onMessage(parsedMessage);
	}
}

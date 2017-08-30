package io.rapid;

import android.content.Context;

import org.json.JSONException;

import io.rapid.executor.RapidExecutor;


abstract class WebSocketConnection {
	protected RapidExecutor mExecutor;
	String mServerURI;
	WebSocketConnectionListener mListener;


	interface WebSocketConnectionListener {
		void onOpen();
		void onMessage(Message message);
		void onClose(CloseReason reason);
//		void onCollectionError(Exception ex);
	}


	WebSocketConnection(String serverURI, WebSocketConnectionListener listener, RapidExecutor executor) {
		mServerURI = serverURI;
		mListener = listener;
		mExecutor = executor;
	}


	abstract void connectToServer(Context context);
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

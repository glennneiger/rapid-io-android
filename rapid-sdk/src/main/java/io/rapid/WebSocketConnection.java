package io.rapid;

import org.json.JSONException;


/**
 * Created by Leos on 19.04.2017.
 */

abstract class WebSocketConnection
{
	String mServerURI;
	WebSocketConnectionListener mListener;


	interface WebSocketConnectionListener
	{
		void onOpen();
		void onMessage(Message message);
		void onClose(CloseReasonEnum reason);
		void onError(Exception ex);
	}


	public WebSocketConnection(String serverURI, WebSocketConnectionListener listener)
	{
		mServerURI = serverURI;
		mListener = listener;
	}


	abstract void connectToServer();
	abstract void sendMessage(String message);


	void disconnectFromServer(boolean sendDisconnectMessage)
	{
		if(sendDisconnectMessage)
		{
			try
			{
				sendMessage(new Message.Dis().toJson().toString());
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
	}


	void handleNewMessage(Message parsedMessage) {
		if(mListener != null) mListener.onMessage(parsedMessage);
	}
}

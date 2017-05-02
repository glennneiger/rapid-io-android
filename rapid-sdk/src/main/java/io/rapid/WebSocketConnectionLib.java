package io.rapid;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


class WebSocketConnectionLib extends WebSocketConnection {

	WebSocketClient mClient;


	public WebSocketConnectionLib(String serverURI, WebSocketConnectionListener listener)
	{
		super(serverURI, listener);
	}


	@Override
	void connectToServer()
	{
		mClient = new WebSocketClient(URI.create(mServerURI))
		{
			@Override
			public void onOpen(ServerHandshake handshakeData)
			{
				Logcat.d("Status message: " + handshakeData.getHttpStatusMessage() + "; HTTP status: " + handshakeData.getHttpStatus());
				if(mListener != null) mListener.onOpen();
			}


			@Override
			public void onMessage(String messageJson) {
				Logcat.d(messageJson);
				try {
					Message parsedMessage = MessageParser.parse(messageJson);

					if(parsedMessage.getMessageType() == MessageType.BATCH) {
						for(Message message : ((Message.Batch) parsedMessage).getMessageList()) {
							handleNewMessage(message);
						}
					} else {
						handleNewMessage(parsedMessage);
					}
				} catch(Exception e) {
					throw new Error(e);
				}
			}


			@Override
			public void onClose(int code, String reason, boolean remote)
			{
				Logcat.d("Code: " + code + "; reason: " + reason + "; remote:" + Boolean.toString(remote));
				CloseReasonEnum reasonEnum = CloseReasonEnum.get(code);
				if(mListener != null) mListener.onClose(reasonEnum);
			}


			@Override
			public void onError(Exception ex)
			{
				ex.printStackTrace();
				if(mListener != null) mListener.onError(ex);
			}


			@Override
			public void onClosing(int code, String reason, boolean remote)
			{
				super.onClosing(code, reason, remote);

				Logcat.d("Code: " + code + "; reason: " + reason + "; remote:" + Boolean.toString(remote));
				CloseReasonEnum reasonEnum = CloseReasonEnum.get(code);
				if(mListener != null) mListener.onClose(reasonEnum);
			}
		};
		mClient.connect();
	}


	@Override
	void sendMessage(String message)
	{
		if(mClient != null){
			Logcat.d(message);
			mClient.send(message);
		}
	}


	@Override
	void disconnectFromServer(boolean sendDisconnectMessage)
	{
		super.disconnectFromServer(sendDisconnectMessage);
		if(mClient != null) mClient.close();
	}
}

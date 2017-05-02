package io.rapid;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import io.rapid.utility.BackgroundExecutor;


/**
 * Created by Leos on 19.04.2017.
 */

public class WebSocketConnectionAsync extends WebSocketConnection {
	WebSocket mClient;


	public WebSocketConnectionAsync(String serverURI, WebSocketConnectionListener listener) {
		super(serverURI, listener);
	}


	@Override
	void connectToServer() {
		BackgroundExecutor.doInBackground(() -> {
			AsyncHttpClient.getDefaultInstance().websocket(mServerURI, "websocket", (ex, webSocket) ->
			{
				if(ex != null) {
					ex.printStackTrace();
					if(mListener != null) mListener.onError(ex);
					return;
				}

				mClient = webSocket;


				webSocket.setStringCallback(messageJson ->
				{
					Logcat.pd("<--- %s", messageJson);
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
				});

				webSocket.setClosedCallback(ex1 ->
				{
					CloseReasonEnum reasonEnum = CloseReasonEnum.get(1);
					if(mListener != null) mListener.onClose(reasonEnum);
				});

				if(mListener != null) mListener.onOpen();
			});
		});
	}


	@Override
	void sendMessage(String message) {
		if(mClient != null) {
			Logcat.pd("---> %s", message);
			mClient.send(message);
		}
	}


	@Override
	void disconnectFromServer(boolean sendDisconnectMessage) {
		super.disconnectFromServer(sendDisconnectMessage);
		if(mClient != null) mClient.close();
	}
}

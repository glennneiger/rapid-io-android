package io.rapid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import io.rapid.executor.RapidExecutor;


@SuppressWarnings("unused")
class WebSocketConnectionLib extends WebSocketConnection {

	@Nullable private WebSocketClient mClient;


	public WebSocketConnectionLib(String serverURI, WebSocketConnectionListener listener, RapidExecutor executor) {
		super(serverURI, listener, executor);
	}


	@Override
	void connectToServer(Context context) {
		mClient = new WebSocketClient(URI.create(mServerURI)) {
			@Override
			public void onOpen(@NonNull ServerHandshake handshakeData) {
				Logcat.d("Status message: " + handshakeData.getHttpStatusMessage() + "; HTTP status: " + handshakeData.getHttpStatus());
				if(mListener != null) mListener.onOpen();
			}


			@Override
			public void onMessage(@NonNull String messageJson) {
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
			public void onClose(int code, String reason, boolean remote) {
				Logcat.d("Code: " + code + "; reason: " + reason + "; remote:" + Boolean.toString(remote));
				CloseReason reasonEnum = CloseReason.get(code);
				if(mListener != null) mListener.onClose(reasonEnum);
			}


			@Override
			public void onError(@NonNull Exception ex) {
				ex.printStackTrace();
//				if(mListener != null) mListener.onError(ex);
			}


			@Override
			public void onClosing(int code, String reason, boolean remote) {
				super.onClosing(code, reason, remote);

				Logcat.d("Code: " + code + "; reason: " + reason + "; remote:" + Boolean.toString(remote));
				CloseReason reasonEnum = CloseReason.get(code);
				if(mListener != null) mListener.onClose(reasonEnum);
			}
		};
		mClient.connect();
	}


	@Override
	void sendMessage(@NonNull String message) {
		if(mClient != null) {
			Logcat.d(message);
			mClient.send(message);
		}
	}


	@Override
	void disconnectFromServer(boolean sendDisconnectMessage) {
		super.disconnectFromServer(sendDisconnectMessage);
		if(mClient != null) mClient.close();
	}
}

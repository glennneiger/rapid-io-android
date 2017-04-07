package io.rapid;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;

import java.net.URI;
import java.util.Map;


/**
 * Created by Leos on 15.03.2017.
 */

class WebSocketConnection extends WebSocketClient {

	private WebSocketConnectionListener mListener;

	interface WebSocketConnectionListener {
		void onOpen();
		void onMessage(Message message);
		void onClose(CloseReasonEnum reason);
		void onError(Exception ex);
	}


	public WebSocketConnection(URI serverURI, WebSocketConnectionListener listener) {
		super(serverURI);
		mListener = listener;
	}


	public WebSocketConnection(URI serverUri, Draft draft, WebSocketConnectionListener listener) {
		super(serverUri, draft);
		mListener = listener;
	}


	public WebSocketConnection(URI serverUri, Draft draft, Map<String, String> headers, int connectTimeout, WebSocketConnectionListener listener) {
		super(serverUri, draft, headers, connectTimeout);
		mListener = listener;
	}


	@Override
	public void onOpen(ServerHandshake handshakeData) {
		Logcat.d("Status message: " + handshakeData.getHttpStatusMessage() + "; HTTP status: " + handshakeData.getHttpStatus());
		if(mListener != null) mListener.onOpen();
	}


	@Override
	public void onMessage(String messageJson) {
		Logcat.d(messageJson);
		new Thread(() ->
		{
			try {
				Message parsedMessage = MessageParser.parse(messageJson);

				if(parsedMessage.getMessageType() == MessageType.BATCH) {
					for(Message message : ((Message.Batch) parsedMessage).getMessageList()) {
						handleNewMessage(message);
					}
				} else {
					handleNewMessage(parsedMessage);
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}).start();
	}


	@Override
	public void onClose(int code, String reason, boolean remote) {
		Logcat.d("Code: " + code + "; reason: " + reason + "; remote:" + Boolean.toString(remote));
		CloseReasonEnum reasonEnum = CloseReasonEnum.get(code);
		if(mListener != null) mListener.onClose(reasonEnum);
	}


	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
		if(mListener != null) mListener.onError(ex);
	}


	private void handleNewMessage(Message parsedMessage) {
		if(mListener != null) mListener.onMessage(parsedMessage);
	}


	void sendMessage(Message message) {
		try {
			String json = message.toJson().toString();
			Logcat.d(json);
			send(json);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}


	void disconnectFromServer(boolean sendDisconnectMessage) {
		if(sendDisconnectMessage) sendMessage(new Message.Dis());
		close();
	}


	void connectToServer() {
		connect();
	}
}

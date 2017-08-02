package io.rapid;

import android.content.Context;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import io.rapid.executor.RapidExecutor;
import io.rapid.utility.NetworkUtility;


class WebSocketConnectionAsync extends WebSocketConnection {
	private WebSocket mClient;


	WebSocketConnectionAsync(String serverURI, WebSocketConnectionListener listener, RapidExecutor executor) {
		super(serverURI, listener, executor);
	}


	@Override
	void connectToServer(Context context) {
		if(NetworkUtility.isOnline(context))
		{
			mExecutor.doInBackground(new Runnable() {
				@Override
				public void run() {
					AsyncHttpClient.getDefaultInstance().websocket(mServerURI, "websocket", new AsyncHttpClient.WebSocketConnectCallback() {
						@Override
						public void onCompleted(Exception ex, WebSocket webSocket) {
							if(ex != null) {
								ex.printStackTrace();
								CloseReason reasonEnum = CloseReason.get(ex);
								if(mListener != null) mListener.onClose(reasonEnum);
								return;
							}

							mClient = webSocket;
							webSocket.setStringCallback(new WebSocket.StringCallback() {
								@Override
								public void onStringAvailable(String messageJson) {
									Logcat.d("<--- %s", messageJson);
									try {
										Message parsedMessage = MessageParser.parse(messageJson);

										if(parsedMessage.getMessageType() == MessageType.BATCH) {
											for(Message message : ((Message.Batch) parsedMessage).getMessageList()) {
												WebSocketConnectionAsync.this.handleNewMessage(message);
											}
										} else {
											WebSocketConnectionAsync.this.handleNewMessage(parsedMessage);
										}
									} catch(Exception e) {
										throw new Error(e);
									}
								}
							});

							webSocket.setClosedCallback(new CompletedCallback() {
								@Override
								public void onCompleted(Exception ex1) {
									if(ex1 != null) ex1.printStackTrace();
									CloseReason reasonEnum = CloseReason.get(ex1);
									if(mListener != null) mListener.onClose(reasonEnum);
								}
							});

							if(mListener != null) mListener.onOpen();
						}
					});
				}
			});
		}
		else
		{
			if(mListener != null) mListener.onClose(CloseReason.NO_INTERNET_CONNECTION);
		}
	}


	@Override
	void sendMessage(String message) {
		if(mClient != null) {
			Logcat.d("---> %s", message);
			mClient.send(message);
		}
	}


	@Override
	void disconnectFromServer(boolean sendDisconnectMessage) {
		super.disconnectFromServer(sendDisconnectMessage);
		if(mClient != null) mClient.close();
	}
}

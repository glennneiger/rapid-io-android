package io.rapid;

import okhttp3.*;
import okio.ByteString;

public class WebsocketConnectionOk extends WebSocketConnection {

    private final OkHttpClient mClient;
    private WebSocket mWS;

    public WebsocketConnectionOk(String serverURI, RapidExecutor executor, WebSocketConnectionListener listener) {
        super(serverURI, executor, listener);
        mClient = new OkHttpClient();
    }

    @Override
    void connectToServer() {
        mExecutor.doInBackground(() -> {

            Request request = new Request.Builder()
                    .url(mServerURI)
                    .build();
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    super.onOpen(webSocket, response);
                    if (mListener != null) mListener.onOpen();
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Logcat.d("<--- %s", text);
                    try {
                        Message parsedMessage = MessageParser.parse(text);

                        if (parsedMessage.getMessageType() == MessageType.BATCH) {
                            for (Message message : ((Message.Batch) parsedMessage).getMessageList()) {
                                handleNewMessage(message);
                            }
                        } else {
                            handleNewMessage(parsedMessage);
                        }
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    super.onMessage(webSocket, bytes);
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    super.onClosing(webSocket, code, reason);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    super.onClosed(webSocket, code, reason);
                    CloseReason reasonEnum = CloseReason.get(code);
                    if (mListener != null) mListener.onClose(reasonEnum);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    CloseReason reasonEnum = CloseReason.get(t);
                    if (mListener != null) mListener.onClose(reasonEnum);
                }
            };
            mWS = mClient.newWebSocket(request, listener);

        });
    }

    @Override
    void sendMessage(String message) {
        if(mWS != null) {
			Logcat.d("---> %s", message);
			mWS.send(message);
		}
    }
}

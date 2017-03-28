package io.rapid;

import android.os.Handler;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.rapid.ConnectionState.CLOSED;
import static io.rapid.ConnectionState.CONNECTED;
import static io.rapid.ConnectionState.CONNECTING;
import static io.rapid.ConnectionState.DISCONNECTED;


/**
 * Created by Leos on 15.03.2017.
 */

class WebSocketConnection extends WebSocketClient {
	private final int HB_TIMEOUT = 10 * 1000;

	private WebSocketConnectionListener mListener;
	private ConnectionState mConnectionState = DISCONNECTED;
	private String mConnectionId;
	private List<MessageBase> mPendingMessageList = new ArrayList<>();
	private Handler mHBHandler = new Handler();
	private Runnable mHBRunnable = () ->
	{
		sendHB();
		startHB();
	};


	enum CloseReasonEnum {
		UNKNOWN(Integer.MAX_VALUE), INTERNET_CONNECTION_LOST(1006), NO_INTERNET_CONNECTION(-1), CLOSED_MANUALLY(1000);

		private int mCode;


		CloseReasonEnum(int code) {
			mCode = code;
		}


		static CloseReasonEnum get(int code) {
			for(CloseReasonEnum item : CloseReasonEnum.values()) {
				if(item.getCode() == code) {
					return item;
				}
			}
			return UNKNOWN;
		}


		public int getCode() {
			return mCode;
		}

	}


	interface WebSocketConnectionListener {
		void onOpen();
		void onMessage(MessageBase message);
		void onClose(CloseReasonEnum reason);
		void onError(Exception ex);
		void onConnectionStateChange(ConnectionState state);
	}


	public WebSocketConnection(String connectionId, URI serverURI, WebSocketConnectionListener listener) {
		super(serverURI);
		mConnectionId = connectionId;
		mListener = listener;
	}


	public WebSocketConnection(String connectionId, URI serverUri, Draft draft, WebSocketConnectionListener listener) {
		super(serverUri, draft);
		mConnectionId = connectionId;
		mListener = listener;
	}


	public WebSocketConnection(String connectionId, URI serverUri, Draft draft, Map<String, String> headers, int connectTimeout, WebSocketConnectionListener listener) {
		super(serverUri, draft, headers, connectTimeout);
		mConnectionId = connectionId;
		mListener = listener;
	}


	@Override
	public void onOpen(ServerHandshake handshakeData) {
		Logcat.d("Status message: " + handshakeData.getHttpStatusMessage() + "; HTTP status: " + handshakeData.getHttpStatus());

		changeConnectionState(CONNECTED);
		sendConnect();
		startHB();

		if(mListener != null) mListener.onOpen();

		for(int i = mPendingMessageList.size() - 1; i >= 0; i--) {
			sendMessage(mPendingMessageList.remove(i));
		}
	}


	@Override
	public void onMessage(String messageJson) {
		Logcat.d(messageJson);

		new Thread(() ->
		{
			try {
				MessageBase parsedMessage = MessageParser.parse(messageJson);

				if(parsedMessage.getMessageType() == MessageBase.MessageType.BATCH) {
					for(MessageBase message : ((MessageBatch) parsedMessage).getMessageList()) {
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

		changeConnectionState(CLOSED);
		stopHB();

		CloseReasonEnum reasonEnum = CloseReasonEnum.get(code);
		if(mListener != null) mListener.onClose(reasonEnum);
	}


	@Override
	public void onError(Exception ex) {
		Logcat.d(ex.getMessage());

		changeConnectionState(DISCONNECTED);
		stopHB();
		if(mListener != null) mListener.onError(ex);
	}


	public void connectToServer() {
		if(getConnectionState() == DISCONNECTED) {
			changeConnectionState(CONNECTING);
			connect();
		}
	}


	public void disconnectFromServer(boolean sendDisconnectMessage) {
		if(sendDisconnectMessage) sendDisconnect();
		close();
	}


	public void sendMessage(MessageBase message) {
		if(getConnectionState() == ConnectionState.CONNECTED) {
			try {
				String json = message.toJson().toString();
				Logcat.d(json);
				send(json);
			} catch(JSONException e) {
				e.printStackTrace();
			}
		} else {
			mPendingMessageList.add(message);
		}
	}


	public ConnectionState getConnectionState() {
		return mConnectionState;
	}


	private void changeConnectionState(ConnectionState state) {
		mConnectionState = state;
		if(mListener != null) mListener.onConnectionStateChange(state);
	}


	private void sendConnect() {
		sendMessage(new MessageCon(IdProvider.getNewEventId(), mConnectionId));
	}


	private void sendDisconnect() {
		sendMessage(new MessageDis(IdProvider.getNewEventId()));
	}


	private void sendAckIfNeeded(MessageBase parsedMessage) {
		if(parsedMessage.getMessageType() == MessageBase.MessageType.VAL || parsedMessage.getMessageType() == MessageBase.MessageType.UPD) {
			sendMessage(new MessageAck(parsedMessage.getEventId()));
		}
	}


	private void handleNewMessage(MessageBase parsedMessage) {
		sendAckIfNeeded(parsedMessage);

		if(parsedMessage.getMessageType() == MessageBase.MessageType.ERR) {
			handleErrorMessage(parsedMessage);
		}

		if(mListener != null) mListener.onMessage(parsedMessage);
	}


	private void handleErrorMessage(MessageBase parsedMessage) {

	}


	private void sendHB() {
		sendMessage(new MessageHb(IdProvider.getNewEventId()));
	}


	private void startHB() {
		stopHB();
		mHBHandler.postDelayed(mHBRunnable, HB_TIMEOUT);
	}


	private void stopHB() {
		mHBHandler.removeCallbacks(mHBRunnable);
	}
}

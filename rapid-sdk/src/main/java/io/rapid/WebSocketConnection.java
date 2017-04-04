package io.rapid;

import android.os.Handler;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.rapid.Config.HB_PERIOD;
import static io.rapid.ConnectionState.CLOSED;
import static io.rapid.ConnectionState.CONNECTED;
import static io.rapid.ConnectionState.CONNECTING;
import static io.rapid.ConnectionState.DISCONNECTED;


/**
 * Created by Leos on 15.03.2017.
 */

class WebSocketConnection extends WebSocketClient {
	private final int MESSAGE_TIMEOUT_PERIOD = 10 * 1000;
	private boolean mReconnect = false;

	private WebSocketConnectionListener mListener;
	private ConnectionState mConnectionState = DISCONNECTED;
	private String mConnectionId;
	private List<Message> mPendingMessageList = new ArrayList<>();
	private List<Message> mSentMessageList = new ArrayList<>();
	private Handler mHBHandler = new Handler();
	private long mLastCommunticationTimestamp = 0;
	private Runnable mHBRunnable = () -> {
		if(System.currentTimeMillis() - mLastCommunticationTimestamp >= HB_PERIOD)
			sendHB();
		startHB();
	};
	private Handler mTimeoutHandler = new Handler();
	private Runnable mTimeoutRunnable = () -> {
		checkMessageTimeout();
		startMessageTimeout();
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
		void onMessage(Message message);
		void onClose(CloseReasonEnum reason);
		void onError(Exception ex);
		void onConnectionStateChange(ConnectionState state);
	}


	public WebSocketConnection(String connectionId, URI serverURI, boolean reconnect, WebSocketConnectionListener listener) {
		super(serverURI);
		mConnectionId = connectionId;
		mListener = listener;
		mReconnect = reconnect;
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
		startMessageTimeout();
		sendConnect();
		startHB();

		if(mListener != null) mListener.onOpen();

		for(int i = mPendingMessageList.size() - 1; i >= 0; i--) {
			sendMessage(mPendingMessageList.remove(i));
		}
	}


	@Override
	public void onMessage(String messageJson) {
		mLastCommunticationTimestamp = System.currentTimeMillis();
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

		changeConnectionState(CLOSED);
		stopHB();
		stopMessageTimeout();

		CloseReasonEnum reasonEnum = CloseReasonEnum.get(code);
		if(mListener != null) mListener.onClose(reasonEnum);
	}


	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();

		changeConnectionState(DISCONNECTED);
		stopHB();
		stopMessageTimeout();
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
		changeConnectionState(CLOSED);
		close();
	}


	public void sendMessage(Message message) {
		if(getConnectionState() == ConnectionState.CONNECTED) {
			try {
				message.setSentTimestamp(new Date().getTime());
				if(message.getMessageType() != MessageType.ACK && message.getMessageType() != MessageType.NOP) mSentMessageList.add(message);
				String json = message.toJson().toString();
				Logcat.d(json);
				send(json);
				mLastCommunticationTimestamp = System.currentTimeMillis();
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
		sendMessage(new Message.Con(mConnectionId, mReconnect));
	}


	private void sendDisconnect() {
		sendMessage(new Message.Dis());
	}


	private void sendAckIfNeeded(Message parsedMessage) {
		if(parsedMessage.getMessageType() == MessageType.VAL || parsedMessage.getMessageType() == MessageType.UPD) {
			sendMessage(new Message.Ack(parsedMessage.getEventId()));
		}
	}


	private void handleNewMessage(Message parsedMessage) {
		sendAckIfNeeded(parsedMessage);

		if(parsedMessage.getMessageType() == MessageType.ERR) {
			handleErrorMessage((Message.Err) parsedMessage);
		} else if(parsedMessage.getMessageType() == MessageType.ACK) {
			handleAckMessage((Message.Ack) parsedMessage);
		}

		if(mListener != null) mListener.onMessage(parsedMessage);
	}


	private void handleErrorMessage(Message.Err parsedMessage) {

	}


	private void handleAckMessage(Message.Ack ackMessage) {
		for(int i = 0; i < mSentMessageList.size(); i++) {
			if(ackMessage.getEventId().equals(mSentMessageList.get(i).getEventId())) {
				if(i == mSentMessageList.size() - 1) mSentMessageList.clear();
				else mSentMessageList = mSentMessageList.subList(i + 1, mSentMessageList.size());
			}
		}
	}


	private void sendHB() {
		sendMessage(new Message.Nop());
	}


	private void startHB() {
		stopHB();
		long nextHb = Config.HB_PERIOD - (System.currentTimeMillis() - mLastCommunticationTimestamp);
		mHBHandler.postDelayed(mHBRunnable, nextHb);
	}


	private void stopHB() {
		mHBHandler.removeCallbacks(mHBRunnable);
	}


	private void checkMessageTimeout() {
		Logcat.d(mSentMessageList.size() + "");

		long now = new Date().getTime();
		for(Message msg : mSentMessageList) {
			if(now - msg.getSentTimestamp() > Config.MESSAGE_TIMEOUT) {
				// TODO message after timeout, handle error
			}
		}
	}


	private void startMessageTimeout() {
		stopMessageTimeout();
		mTimeoutHandler.postDelayed(mTimeoutRunnable, MESSAGE_TIMEOUT_PERIOD);
	}


	private void stopMessageTimeout() {
		mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
	}
}

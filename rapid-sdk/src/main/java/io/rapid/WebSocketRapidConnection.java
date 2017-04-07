package io.rapid;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.rapid.Config.CHECKER_HANDLER_PERIOD;
import static io.rapid.Config.HB_PERIOD;
import static io.rapid.ConnectionState.CLOSED;
import static io.rapid.ConnectionState.CONNECTED;
import static io.rapid.ConnectionState.DISCONNECTED;


class WebSocketRapidConnection extends RapidConnection implements WebSocketConnection.WebSocketConnectionListener {

	private final Context mContext;
	private final Handler mOriginalThreadHandler;
	private WebSocketConnection mWebSocketConnection;
	private boolean mInternetConnected = true;
	private ConnectionState mConnectionState = DISCONNECTED;
	private String mConnectionId;
	private boolean mReconnect = false;
	private long mLastCommunicationTimestamp = 0;
	private int mSubscriptionCount = 0;
	private List<MessageFuture> mPendingMessageList = new ArrayList<>();
	private List<MessageFuture> mSentMessageList = new ArrayList<>();
	private Handler mCheckHandler = new Handler();
	private Runnable mCheckRunnable = () -> {
		check();
		startCheckHandler();
	};
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();



	private BroadcastReceiver mInternetConnectionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				final NetworkInfo info = connManager.getActiveNetworkInfo();
				boolean hasInternetConnection = info != null && info.isConnected();
				Logcat.d("Internet connected: " + hasInternetConnection);
				if(hasInternetConnection) {
					unregisterInternetConnectionBroadcast();
					createNewWebSocketConnection();
					mInternetConnected = true;
				}
			}
		}

	};


	public WebSocketRapidConnection(Context context, Handler originalThreadHandler, Callback rapidConnectionCallback) {
		super(rapidConnectionCallback);
		mContext = context;
		mOriginalThreadHandler = originalThreadHandler;
	}


	@Override
	public void onOpen() {
		changeConnectionState(CONNECTED);
		startCheckHandler();
		sendConnect();

		for(int i = mPendingMessageList.size() - 1; i >= 0; i--) {
			sendMessage(mPendingMessageList.remove(i));
		}
	}


	@Override
	public void onMessage(Message message) {
		mLastCommunicationTimestamp = System.currentTimeMillis();
		sendAckIfNeeded(message);

		if(message.getMessageType() == MessageType.ERR) {
			switch(((Message.Err) message).getErrorType()) {
				case CONNECTION_TERMINATED:
					mOriginalThreadHandler.post(() -> {
						disconnectFromServer(false);
						createNewWebSocketConnection();
						reconnectSubscriptions();
					});
					break;
			}
		}
		else if(message.getMessageType() == MessageType.ACK) {
			handleAckMessage((Message.Ack) message);
		}
		else if(message.getMessageType() == MessageType.VAL) {
			Message.Val valMessage = ((Message.Val) message);
			mCallback.onValue(valMessage.getSubscriptionId(), valMessage.getCollectionId(), valMessage.getDocuments());
		} else if(message.getMessageType() == MessageType.UPD) {
			Message.Upd updMessage = ((Message.Upd) message);
			mCallback.onUpdate(updMessage.getSubscriptionId(), updMessage.getCollectionId(), updMessage.getPreviousSiblingId(), updMessage.getDocument());
		}
	}


	@Override
	public void onClose(CloseReasonEnum reason) {
		startCheckHandler();
		changeConnectionState(CLOSED);
		if(reason == CloseReasonEnum.INTERNET_CONNECTION_LOST ||
				reason == CloseReasonEnum.NO_INTERNET_CONNECTION) {
			mInternetConnected = false;
			registerInternetConnectionBroadcast();
		}
	}


	@Override
	public void onError(Exception ex) {
		changeConnectionState(DISCONNECTED);
		stopCheckHandler();
	}


	@Override
	public void addConnectionStateListener(RapidConnectionStateListener listener) {
		mConnectionStateListeners.add(listener);
	}


	@Override
	public void removeConnectionStateListener(RapidConnectionStateListener listener) {
		mConnectionStateListeners.remove(listener);
	}


	@Override
	public void removeAllConnectionStateListeners() {
		mConnectionStateListeners.clear();
	}


	@Override
	public ConnectionState getConnectionState() {
		return mConnectionState;
	}


	@Override
	void subscribe(String subscriptionId, Subscription subscription) {
		Message.Sub messageSub = new Message.Sub(subscription.getCollectionName(), subscriptionId);
		messageSub.setFilter(subscription.getFilter());
		messageSub.setLimit(subscription.getLimit());
		messageSub.setOrder(subscription.getOrder());
		messageSub.setSkip(subscription.getSkip());

		if(mInternetConnected && (mWebSocketConnection == null || getConnectionState() == CLOSED)) {
			createNewWebSocketConnection();
		}

		sendMessage(messageSub);

		mSubscriptionCount++;
	}


	@Override
	public void onUnsubscribe(Subscription subscription) {
		Message.Uns messageUns = new Message.Uns(subscription.getSubscriptionId());
		mSubscriptionCount--;
		sendMessage(messageUns).onCompleted(() -> {
			if(mSubscriptionCount == 0) {
				disconnectFromServer(true);
			}
		});
	}


	@Override
	public RapidFuture mutate(String collectionName, String documentJson) {
		return sendMessage(new Message.Mut(collectionName, documentJson));
	}


	private void registerInternetConnectionBroadcast() {
		if(mContext != null)
			mContext.registerReceiver(mInternetConnectionBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}


	private void unregisterInternetConnectionBroadcast() {
		if(mContext != null) mContext.unregisterReceiver(mInternetConnectionBroadcastReceiver);
	}


	private void invokeConnectionStateChanged(ConnectionState state) {
		for(RapidConnectionStateListener l : mConnectionStateListeners) {
			if(l != null) l.onConnectionStateChanged(state);
		}
	}


	private void disconnectFromServer(boolean sendDisconnectMessage) {
		mWebSocketConnection.disconnectFromServer(sendDisconnectMessage);
		changeConnectionState(CLOSED);
		mConnectionId = null;
	}


	private void createNewWebSocketConnection() {
		mReconnect = true;
		if(mConnectionId == null) {
			mConnectionId = IdProvider.getConnectionId();
			mReconnect = false;
		}
		mWebSocketConnection = new WebSocketConnection(URI.create(Config.URI), this);
		mWebSocketConnection.connectToServer();
	}


	private void reconnectSubscriptions() {
		getCallback().onReconnected();
	}


	public RapidFuture sendMessage(Message message) {
		RapidFuture future = new RapidFuture();
		if(mConnectionState == CONNECTED) {
			sendMessage(new MessageFuture(message, future));
		} else {
			mPendingMessageList.add(new MessageFuture(message, future));
		}
		return future;
	}


	private void sendMessage(MessageFuture msg) {
		if(msg.getMessage().getMessageType() != MessageType.ACK && msg.getMessage().getMessageType() != MessageType.NOP)
			mSentMessageList.add(msg);
		mWebSocketConnection.sendMessage(msg.getMessage());
		mLastCommunicationTimestamp = System.currentTimeMillis();
	}


	private void changeConnectionState(ConnectionState state) {
		Logcat.d(state.name());
		mConnectionState = state;
		invokeConnectionStateChanged(state);
	}


	private void sendConnect() {
		sendMessage(new Message.Con(mConnectionId, mReconnect));
	}


	private void sendAckIfNeeded(Message parsedMessage) {
		if(parsedMessage.getMessageType() == MessageType.VAL || parsedMessage.getMessageType() == MessageType.UPD) {
			sendMessage(new Message.Ack(parsedMessage.getEventId()));
		}
	}


	private synchronized void handleAckMessage(Message.Ack ackMessage) {
		for(int i = 0; i < mSentMessageList.size(); i++) {
			if(ackMessage.getEventId().equals(mSentMessageList.get(i).getMessage().getEventId())) {
				if(i == mSentMessageList.size() - 1) {
					for(int j = 0; j < mSentMessageList.size(); j++) {
						MessageFuture messageFuture = mSentMessageList.get(j);
						messageFuture.getRapidFuture().invokeSuccess();
					}
					mSentMessageList.clear();
				} else {
					for(int j = 0; j <= i; j++) {
						mSentMessageList.get(j).getRapidFuture().invokeSuccess();
					}
					mSentMessageList = mSentMessageList.subList(i + 1, mSentMessageList.size());
				}
			}
		}
	}


	private void sendHB() {
		sendMessage(new Message.Nop());
	}


	private void checkMessageTimeout() {
		Logcat.d("Pending list: " + mPendingMessageList.size());
		Logcat.d("Sent list: " + mSentMessageList.size());

		long now = new Date().getTime();
		for(MessageFuture mf : mSentMessageList) {
			if(now - mf.getSentTimestamp() > Config.MESSAGE_TIMEOUT) {
				mf.getRapidFuture().invokeError(new RapidError(RapidError.TIMEOUT));
			}
		}
		for(MessageFuture mf : mPendingMessageList) {
			if(now - mf.getSentTimestamp() > Config.MESSAGE_TIMEOUT) {
				mf.getRapidFuture().invokeError(new RapidError(RapidError.TIMEOUT));
			}
		}
	}



	private void startCheckHandler() {
		stopCheckHandler();
		mCheckHandler.postDelayed(mCheckRunnable, CHECKER_HANDLER_PERIOD);
	}


	private void stopCheckHandler() {
		mCheckHandler.removeCallbacks(mCheckRunnable);
	}


	private void check() {
		if(System.currentTimeMillis() - mLastCommunicationTimestamp >= HB_PERIOD)
			sendHB();

		checkMessageTimeout();
	}
}

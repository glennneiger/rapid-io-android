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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class WebSocketRapidConnection extends RapidConnection implements WebSocketConnection.WebSocketConnectionListener {

	private final Context mContext;
	private final Handler mOriginalThreadHandler;
	private WebSocketConnection mWebSocketConnection;
	private Map<String, MessageFuture> mPendingMessages = new HashMap<>();
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();
	private boolean mInternetConnected = true;
	private String mConnectionId;


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
	}


	@Override
	public void onMessage(MessageBase message) {
		if(message.getMessageType() == MessageBase.MessageType.ACK) {
			MessageAck ackMessage = ((MessageAck) message);
			MessageFuture messageFuture = mPendingMessages.remove(ackMessage.getEventId());
			if(messageFuture != null)
				messageFuture.invokeSuccess();
		} else if(message.getMessageType() == MessageBase.MessageType.ERR) {
			switch(((MessageErr) message).getErrorType()) {
				case CONNECTION_TERMINATED:
					mOriginalThreadHandler.post(() -> {
						disconnectFromServer(false);
						createNewWebSocketConnection();
						reconnectSubscriptions();
					});
					break;
			}
		} else {
			getCallback().onMessage(message);
		}
	}


	@Override
	public void onClose(WebSocketConnection.CloseReasonEnum reason) {
		if(reason == WebSocketConnection.CloseReasonEnum.INTERNET_CONNECTION_LOST ||
				reason == WebSocketConnection.CloseReasonEnum.NO_INTERNET_CONNECTION) {
			mInternetConnected = false;
			registerInternetConnectionBroadcast();
		}
	}


	@Override
	public void onError(Exception ex) {

	}


	@Override
	public void onConnectionStateChange(ConnectionState state) {
		Logcat.d(state.name());
		invokeConnectionStateChanged(state);
	}


	@Override
	public MessageFuture sendMessage(MessageBase message) {
		MessageFuture future = new MessageFuture();
		mPendingMessages.put(message.getEventId(), future);
		mWebSocketConnection.sendMessage(message);
		return future;
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
		return mWebSocketConnection.getConnectionState();
	}


	@Override
	public void onSubscribe() {
		// some subscription subscribed - connect if not connected
		if(mInternetConnected && (mWebSocketConnection == null || mWebSocketConnection.getConnectionState() == ConnectionState.CLOSED)) {
			createNewWebSocketConnection();
		}
	}


	@Override
	public void onUnsubscribe(boolean lastSubscription) {
		if(lastSubscription) {
			disconnectFromServer(true);
		}
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
		mConnectionId = null;
	}


	private void createNewWebSocketConnection() {
		boolean reconnect = true;
		if(mConnectionId == null) {
			mConnectionId = IdProvider.getConnectionId();
			reconnect = false;
		}
		mWebSocketConnection = new WebSocketConnection(mConnectionId, URI.create(Config.URI), reconnect, this);
		mWebSocketConnection.connectToServer();
	}


	private void reconnectSubscriptions() {
		getCallback().onReconnected();
	}

}

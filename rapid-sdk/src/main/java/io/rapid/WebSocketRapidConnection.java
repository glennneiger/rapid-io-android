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
import java.util.List;


class WebSocketRapidConnection extends RapidConnection implements WebSocketConnection.WebSocketConnectionListener {

	private final Context mContext;
	private final Handler mOriginalThreadHandler;
	private WebSocketConnection mWebSocketConnection;
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
	private int mSubscriptionCount = 0;


	public WebSocketRapidConnection(Context context, Handler originalThreadHandler, Callback rapidConnectionCallback) {
		super(rapidConnectionCallback);
		mContext = context;
		mOriginalThreadHandler = originalThreadHandler;
	}


	@Override
	public void onOpen() {
	}


	@Override
	public void onMessage(Message message) {
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
		} else if(message.getMessageType() == MessageType.VAL) {
			Message.Val valMessage = ((Message.Val) message);
			mCallback.onValue(valMessage.getSubscriptionId(), valMessage.getCollectionId(), valMessage.getDocuments());
		} else if(message.getMessageType() == MessageType.UPD) {
			Message.Upd updMessage = ((Message.Upd) message);
			mCallback.onUpdate(updMessage.getSubscriptionId(), updMessage.getCollectionId(), updMessage.getPreviousSiblingId(), updMessage.getDocument());
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
	void subscribe(String subscriptionId, Subscription subscription) {
		Message.Sub messageSub = new Message.Sub(subscription.getCollectionName(), subscriptionId);
		messageSub.setFilter(subscription.getFilter());
		messageSub.setLimit(subscription.getLimit());
		messageSub.setOrder(subscription.getOrder());
		messageSub.setSkip(subscription.getSkip());

		if(mInternetConnected && (mWebSocketConnection == null || mWebSocketConnection.getConnectionState() == ConnectionState.CLOSED)) {
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


	private RapidFuture sendMessage(Message message) {
		return mWebSocketConnection.sendMessage(message);
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

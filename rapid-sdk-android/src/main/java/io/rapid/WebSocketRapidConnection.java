package io.rapid;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static io.rapid.Config.CHECKER_HANDLER_PERIOD;
import static io.rapid.Config.HB_PERIOD;
import static io.rapid.ConnectionState.CONNECTED;
import static io.rapid.ConnectionState.CONNECTING;
import static io.rapid.ConnectionState.DISCONNECTED;
import static io.rapid.RapidError.ErrorType.SUBSCRIPTION_CANCELLED;
import static io.rapid.RapidError.ErrorType.TIMEOUT;


class WebSocketRapidConnection extends RapidConnection implements WebSocketConnection.WebSocketConnectionListener {
	private final Context mContext;
	private final Handler mOriginalThreadHandler;
	private final String mUrl;
	private final RapidLogger mLogger;
	private long mConnectionTimeout = Config.DEFAULT_CONNECTION_TIMEOUT;
	private WebSocketConnection mWebSocketConnection;
	private long mConnectionLossTimestamp = -1;
	private ConnectionState mConnectionState = DISCONNECTED;
	private String mConnectionId;
	private long mLastCommunicationTimestamp = 0;
	private int mSubscriptionCount = 0;
	private int mPendingMutationCount = 0;
	private List<MessageFuture> mPendingMessageList = new ArrayList<>();
	private List<MessageFuture> mSentMessageList = new ArrayList<>();
	private AuthHelper mAuth;
	private boolean mCheckRunning = false;
	private Handler mCheckHandler = new Handler();
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();
	private Runnable mDisconnectRunnable = () -> {
		disconnectWebSocketConnection(true);
	};
	private Runnable mConnectionRetryRunnable = this::createWebSocketConnectionIfNeeded;
	private Runnable mCheckRunnable = () ->
	{
		startCheckHandler();
		check();
	};
	private Queue<RapidCallback.TimeOffset> mTimeOffsetCallbacks = new LinkedList<>();


	WebSocketRapidConnection(Context context, String url, Callback rapidConnectionCallback, Handler originalThreadHandler, RapidLogger logger) {
		super(rapidConnectionCallback);
		mContext = context;
		mUrl = url;
		mOriginalThreadHandler = originalThreadHandler;
		mLogger = logger;
		mConnectionLossTimestamp = System.currentTimeMillis();

		AuthHelper.AuthCallback authCallback = new AuthHelper.AuthCallback() {
			@Override
			public void sendAuthMessage() {
				createWebSocketConnectionIfNeeded();
				if(mConnectionState == CONNECTED) WebSocketRapidConnection.this.sendAuthMessage();
			}


			@Override
			public void sendDeauthMessage() {
				createWebSocketConnectionIfNeeded();
				if(mConnectionState == CONNECTED) WebSocketRapidConnection.this.sendDeauthMessage();
			}
		};
		mAuth = new AuthHelper(mOriginalThreadHandler, authCallback, mLogger);
	}


	@Override
	public void onOpen() {
		changeConnectionState(CONNECTED);
		startCheckHandler();
		sendConnect();

		if(mAuth.isAuthPending() || mAuth.isAuthRequired()) {
			mAuth.setAuthPending();
			sendAuthMessage();
		}
		if(mAuth.isDeauthPending()) sendDeauthMessage();

		if(!mPendingMessageList.isEmpty()) {
			for(int i = 0; i < mPendingMessageList.size(); i++) {
				sendMessage(mPendingMessageList.get(i));
			}
			mPendingMessageList.clear();
		}
	}


	@Override
	public void onMessage(Message message) {
		mLastCommunicationTimestamp = System.currentTimeMillis();
		sendAckIfNeeded(message);

		if(message.getMessageType() == MessageType.ERR) {
			handleErrMessage((Message.Err) message);
		} else if(message.getMessageType() == MessageType.CA) {
			handleCaMessage((Message.Ca) message);
		} else if(message.getMessageType() == MessageType.CA_CH) {
			handleCaMessage((Message.CaCh) message);
		} else if(message.getMessageType() == MessageType.ACK) {
			handleAckMessage((Message.Ack) message);
		} else if(message.getMessageType() == MessageType.VAL) {
			Message.Val valMessage = ((Message.Val) message);
			mCallback.onValue(valMessage.getSubscriptionId(), valMessage.getCollectionId(), valMessage.getDocuments());
		} else if(message.getMessageType() == MessageType.RES) {
			Message.Res resMessage = ((Message.Res) message);
			handleResMessage(resMessage);
			mCallback.onFetchResult(resMessage.getFetchId(), resMessage.getCollectionId(), resMessage.getDocuments());
		} else if(message.getMessageType() == MessageType.UPD) {
			Message.Upd updMessage = ((Message.Upd) message);
			mCallback.onUpdate(updMessage.getSubscriptionId(), updMessage.getCollectionId(), updMessage.getDocument());
		} else if(message.getMessageType() == MessageType.RM) {
			Message.Rm rmMessage = ((Message.Rm) message);
			mCallback.onRemove(rmMessage.getSubscriptionId(), rmMessage.getCollectionId(), rmMessage.getDocument());
		} else if(message.getMessageType() == MessageType.TS) {
			Message.Ts tsMessage = (Message.Ts) message;
			handleTsMessage(tsMessage);
		} else if(message.getMessageType() == MessageType.MES) {
			Message.Mes mesMessage = (Message.Mes) message;
			mCallback.onChannelMessage(mesMessage.getSubscriptionId(), mesMessage.getChannelName(), mesMessage.getBody());
		}
	}


	@Override
	public void onClose(CloseReason reason) {
		mAuth.onClose();
		if(reason == CloseReason.CLOSED_FROM_SERVER && mConnectionState == DISCONNECTED) {
			reason = CloseReason.CLOSED_MANUALLY;
		}

		if(reason != CloseReason.CLOSED_MANUALLY) {
			mLogger.logE("Connection closed. Reason: %s", reason.name());
			if(mConnectionState == CONNECTED) mConnectionLossTimestamp = System.currentTimeMillis();
			changeConnectionState(CONNECTING);
			mWebSocketConnection = null;
			if(!mCheckRunning)
				startCheckHandler();

			// try to connect again
			mCheckHandler.postDelayed(mConnectionRetryRunnable, Config.CONNECTION_RETRY_PERIOD);
		}
	}


	@Override
	public RapidFuture authorize(String token) {
		return mAuth.authorize(token);
	}


//	@Override
//	public void onCollectionError(Exception ex)
//	{
//		mLogger.logE(ex);
//		changeConnectionState(DISCONNECTED);
//		mWebSocketConnection = null;
//		stopCheckHandler();
//		//TODO invoke error to user
//	}


	@Override
	public RapidFuture deauthorize() {
		return mAuth.deauthorize(mConnectionState);
	}


	@Override
	boolean isAuthenticated() {
		return mAuth.isAuthenticated();
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
	void subscribe(BaseCollectionSubscription subscription) {
		Message.Sub messageSub = new Message.Sub(subscription.getCollectionName(), subscription.getSubscriptionId());
		messageSub.setFilter(subscription.getFilter());
		messageSub.setLimit(subscription.getLimit());
		messageSub.setOrder(subscription.getOrder());
		messageSub.setSkip(subscription.getSkip());

		mSubscriptionCount++;
		createWebSocketConnectionIfNeeded();
		RapidFuture future = sendMessage(() -> messageSub);
		future.onError(error -> mCallback.onCollectionError(subscription.getSubscriptionId(), subscription.getCollectionName(), error));
	}


	@Override
	public void subscribeChannel(String subscriptionId, RapidChannelSubscription subscription, boolean nameIsPrefix) {
		Message.SubCh messageSub = new Message.SubCh(subscription.getChannelName(), subscription.getSubscriptionId(), nameIsPrefix);
		mSubscriptionCount++;
		createWebSocketConnectionIfNeeded();
		RapidFuture future = sendMessage(() -> messageSub);
		future.onError(error -> mCallback.onChannelError(subscription.getSubscriptionId(), subscription.getChannelName(), error));
	}


	@Override
	void fetch(String fetchId, BaseCollectionSubscription subscription) {
		Message.Ftc messageFtc = new Message.Ftc(subscription.getCollectionName(), fetchId);
		messageFtc.setFilter(subscription.getFilter());
		messageFtc.setLimit(subscription.getLimit());
		messageFtc.setOrder(subscription.getOrder());
		messageFtc.setSkip(subscription.getSkip());

		mSubscriptionCount++;
		createWebSocketConnectionIfNeeded();
		RapidFuture future = sendMessage(() -> messageFtc);
		future.onError(error -> mCallback.onCollectionError(fetchId, subscription.getCollectionName(), error));
	}


	@Override
	public void onUnsubscribe(BaseCollectionSubscription subscription) {
		boolean sendUnsubscribe = true;
		for(int i = mPendingMessageList.size() - 1; i >= 0; i--) {
			if(mPendingMessageList.get(i).getMessage() instanceof Message.Sub && ((Message.Sub) mPendingMessageList.get(i).getMessage())
					.getSubscriptionId().equals(subscription.getSubscriptionId())) {
				mPendingMessageList.remove(i);
				sendUnsubscribe = false;
				break;
			}
		}

		mSubscriptionCount--;

		if(sendUnsubscribe) {
			Message.Uns messageUns = new Message.Uns(subscription.getSubscriptionId());
			sendMessage(() -> messageUns);
		}
	}


	@Override
	void onUnsubscribe(RapidChannelSubscription subscription) {
		boolean sendUnsubscribe = true;
		for(int i = mPendingMessageList.size() - 1; i >= 0; i--) {
			if(mPendingMessageList.get(i).getMessage() instanceof Message.SubCh && ((Message.SubCh) mPendingMessageList.get(i).getMessage())
					.getSubscriptionId().equals(subscription.getSubscriptionId())) {
				mPendingMessageList.remove(i);
				sendUnsubscribe = false;
				break;
			}
		}

		mSubscriptionCount--;

		if(sendUnsubscribe) {
			Message.UnsCh messageUns = new Message.UnsCh(subscription.getSubscriptionId());
			sendMessage(() -> messageUns);
		}
	}


	@Override
	public RapidFuture mutate(String collectionName, FutureResolver<String> documentJsonResolver) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Mut(collectionName, documentJsonResolver.resolve()));
	}


	@Override
	public RapidFuture merge(String collectionName, FutureResolver<String> documentJsonResolver) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Mer(collectionName, documentJsonResolver.resolve()));
	}


	@Override
	public RapidFuture publish(String channelName, FutureResolver<String> messageJson) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Pub(channelName, messageJson.resolve()));
	}


	@Override
	public RapidFuture delete(String collectionName, FutureResolver<String> documentJsonResolver) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Del(collectionName, documentJsonResolver.resolve()));
	}


	@Override
	public void setConnectionTimeout(long connectionTimeoutMs) {
		mConnectionTimeout = connectionTimeoutMs;
	}


	@Override
	public RapidFuture getSetverTimeOffset(RapidCallback.TimeOffset callback) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		mTimeOffsetCallbacks.add(callback);
		return sendMessage(Message.ReqTs::new);
	}


	@Override
	public RapidActionFuture onDisconnectDelete(String collectionName, FutureResolver<String> documentJsonResolver) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		String actionId = IdProvider.getNewActionId();
		RapidActionFuture future = new RapidActionFuture(mOriginalThreadHandler, actionId, this);
		return (RapidActionFuture) sendMessage(future, () -> new Message.Da(actionId, new Message.Del(collectionName, documentJsonResolver.resolve())));
	}


	@Override
	public RapidActionFuture onDisconnectMutate(String collectionName, FutureResolver<String> documentJsonResolver) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		String actionId = IdProvider.getNewActionId();
		RapidActionFuture future = new RapidActionFuture(mOriginalThreadHandler, actionId, this);
		return (RapidActionFuture) sendMessage(future, () -> new Message.Da(actionId, new Message.Mut(collectionName, documentJsonResolver.resolve())));
	}


	@Override
	public RapidActionFuture onDisconnectMerge(String collectionName, FutureResolver<String> documentJsonResolver) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		String actionId = IdProvider.getNewActionId();
		RapidActionFuture future = new RapidActionFuture(mOriginalThreadHandler, actionId, this);
		return (RapidActionFuture) sendMessage(future, () -> new Message.Da(actionId, new Message.Mer(collectionName, documentJsonResolver.resolve())));
	}


	@Override
	public RapidFuture cancelOnDisconnect(String actionId) {
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.DaCa(actionId));
	}


	private void handleTsMessage(Message.Ts tsMessage) {
		mPendingMutationCount--;
		long diff = System.currentTimeMillis() - tsMessage.getTimestamp();
		mTimeOffsetCallbacks.poll().onTimeOffsetReceived(diff);
	}


	private void handleResMessage(Message.Res resMessage) {
		mSubscriptionCount--;
	}


	private void createWebSocketConnectionIfNeeded() {
		if(!mPendingMessageList.isEmpty() || mPendingMutationCount > 0 || mSubscriptionCount > 0 || mAuth.isAuthPending() || mAuth.isDeauthPending()) {
			// cancel scheduled disconnect if any
			mCheckHandler.removeCallbacks(mDisconnectRunnable);
			if(mWebSocketConnection == null) {
				changeConnectionState(CONNECTING);
				mWebSocketConnection = new WebSocketConnectionAsync(mUrl, this);
				mWebSocketConnection.connectToServer(mContext);
			}
		}
	}


	private void disconnectWebSocketConnectionIfNeeded() {
		if(mSubscriptionCount == 0 && mPendingMutationCount == 0 && !mAuth.isAuthPending() && !mAuth.isDeauthPending()
				&& mPendingMessageList.isEmpty()) {
			Logcat.d("Scheduling websocket disconnection in %d ms", Config.WEBSOCKET_DISCONNECT_TIMEOUT);
			mCheckHandler.postDelayed(mDisconnectRunnable, Config.WEBSOCKET_DISCONNECT_TIMEOUT);
		}
	}


	private void disconnectWebSocketConnection(boolean sendDisconnectMessage) {
		// cancel scheduled disconnect if any
		mCheckHandler.removeCallbacks(mDisconnectRunnable);

		if(mWebSocketConnection != null) {
			changeConnectionState(DISCONNECTED);
			mWebSocketConnection.disconnectFromServer(sendDisconnectMessage);
			mWebSocketConnection = null;
			mConnectionId = null;
			stopCheckHandler();
		}
	}


	private void reconnectSubscriptions() {
		getCallback().onReconnected();
	}


	private RapidFuture sendMessage(FutureResolver<Message> message) {
		RapidFuture future = new RapidFuture(mOriginalThreadHandler);
		return sendMessage(future, message);
	}


	private RapidFuture sendMessage(RapidFuture baseFuture, FutureResolver<Message> message) {
		// send message in background
		new AsyncTask<Void, Void, MessageFuture>() {
			@Override
			protected MessageFuture doInBackground(Void... params) {
				Message m = message.resolve();
				try {
					MessageFuture messageFuture = new MessageFuture(m, m.toJson().toString(), baseFuture);
					if(mConnectionState == CONNECTED) {
						sendMessage(messageFuture);
					} else {
						if(!(messageFuture.getMessage() instanceof Message.Nop)) mPendingMessageList.add(messageFuture);
					}
				} catch(JSONException e) {
					e.printStackTrace();
					return null;
				}
				return null;
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		return baseFuture;
	}


	private void sendMessage(MessageFuture msg) {
		if(msg.getMessage().getMessageType() != MessageType.ACK && msg.getMessage().getMessageType() != MessageType.NOP)
			mSentMessageList.add(msg);
		mWebSocketConnection.sendMessage(msg.getMessageJson());
		mLastCommunicationTimestamp = System.currentTimeMillis();
	}


	private void changeConnectionState(ConnectionState state) {
		Logcat.d(state.name());
		if(mConnectionState != state) {
			mConnectionState = state;
			invokeConnectionStateChanged(state);
		}
	}


	private void invokeConnectionStateChanged(ConnectionState state) {
		for(RapidConnectionStateListener l : mConnectionStateListeners) {
			if(l != null) l.onConnectionStateChanged(state);
		}
	}


	private void sendConnect() {
		boolean reconnect = true;
		if(mConnectionId == null) {
			mConnectionId = IdProvider.getConnectionId();
			reconnect = false;
		}
		boolean finalReconnect = reconnect;

		try {
			Message.Con m = new Message.Con(mConnectionId, finalReconnect);
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			MessageFuture messageFuture = new MessageFuture(m, m.toJson().toString(), future);
			sendMessage(messageFuture);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}


	private void sendAckIfNeeded(Message parsedMessage) {
		if(parsedMessage.getMessageType() == MessageType.VAL
				|| parsedMessage.getMessageType() == MessageType.UPD
				|| parsedMessage.getMessageType() == MessageType.RES
				|| parsedMessage.getMessageType() == MessageType.RM
				|| parsedMessage.getMessageType() == MessageType.TS
				|| parsedMessage.getMessageType() == MessageType.CA_CH
				|| parsedMessage.getMessageType() == MessageType.MES
				|| parsedMessage.getMessageType() == MessageType.CA) {
			sendMessage(() -> new Message.Ack(parsedMessage.getEventId()));
		}
	}


	private synchronized void handleErrMessage(Message.Err message) {
		switch(message.getType()) {
			case CONNECTION_TERMINATED:
				disconnectWebSocketConnection(false);
				mOriginalThreadHandler.post(() ->
				{
					createWebSocketConnectionIfNeeded();
					mSubscriptionCount = 0;
					reconnectSubscriptions();
				});
				break;
			default:
				int size = mSentMessageList.size();
				int position = -1;
				for(int i = 0; i < size; i++) {
					if(message.getEventId().equals(mSentMessageList.get(i).getMessage().getEventId())) {
						position = i;
						break;
					}
				}
				if(position != -1) {
					MessageFuture messageFuture = mSentMessageList.get(position);
					RapidError error = new RapidError(RapidError.ErrorType.fromServerError(message, messageFuture));
					mLogger.logE(error);
					messageFuture.getRapidFuture().invokeError(error);
					updateCountersOnError(messageFuture);
					mSentMessageList.remove(position);
				}

				break;
		}

		disconnectWebSocketConnectionIfNeeded();
	}


	private void updateCountersOnError(MessageFuture erroredMessageFuture) {
		if(erroredMessageFuture.getMessage() instanceof Message.Mut
				|| erroredMessageFuture.getMessage() instanceof Message.Del
				|| erroredMessageFuture.getMessage() instanceof Message.Ts
				|| erroredMessageFuture.getMessage() instanceof Message.Pub
				|| erroredMessageFuture.getMessage() instanceof Message.Da
				|| erroredMessageFuture.getMessage() instanceof Message.DaCa
				|| erroredMessageFuture.getMessage() instanceof Message.Mer)
			mPendingMutationCount--;
		if(erroredMessageFuture.getMessage() instanceof Message.Sub || erroredMessageFuture.getMessage() instanceof Message.Ftc)
			mSubscriptionCount--;
	}


	private void timeoutMessage(MessageFuture messageFuture) {
		updateCountersOnError(messageFuture);
		RapidError error = new RapidError(TIMEOUT);
		mLogger.logE(error);
		messageFuture.getRapidFuture().invokeError(error);
	}


	private synchronized void handleCaMessage(Message.Ca message) {
		mSubscriptionCount--;
		disconnectWebSocketConnectionIfNeeded();
		getCallback().onCollectionError(message.getSubscriptionId(), message.getCollectionId(), new RapidError(SUBSCRIPTION_CANCELLED));
	}


	private void handleCaMessage(Message.CaCh message) {
		mSubscriptionCount--;
		disconnectWebSocketConnectionIfNeeded();
		getCallback().onChannelError(message.getSubscriptionId(), message.getChannelId(), new RapidError(SUBSCRIPTION_CANCELLED));
	}


	private synchronized void handleAckMessage(Message.Ack ackMessage) {
		int size = mSentMessageList.size();

		int position = -1;

		for(int i = 0; i < size; i++) {
			if(ackMessage.getEventId().equals(mSentMessageList.get(i).getMessage().getEventId())) {
				position = i;
				break;
			}
		}
		if(position != -1) {
			MessageFuture messageFuture = mSentMessageList.get(position);
			messageFuture.getRapidFuture().invokeSuccess();
			if(messageFuture.getMessage() instanceof Message.Mut
					|| messageFuture.getMessage() instanceof Message.Del
					|| messageFuture.getMessage() instanceof Message.Mer
					|| messageFuture.getMessage() instanceof Message.Da
					|| messageFuture.getMessage() instanceof Message.DaCa
					|| messageFuture.getMessage() instanceof Message.Pub)
				mPendingMutationCount--;
//			if(messageFuture.getMessage() instanceof Message.Auth) {
//				mAuth.authSuccess();
//			}
//			if(messageFuture.getMessage() instanceof Message.Deauth) {
//				mAuth.deauthSuccess();
//			}
			mSentMessageList.remove(position);
		}

		disconnectWebSocketConnectionIfNeeded();
	}


	private void startCheckHandler() {
		stopCheckHandler();
		mCheckRunning = true;
		mCheckHandler.postDelayed(mCheckRunnable, CHECKER_HANDLER_PERIOD);
	}


	private void stopCheckHandler() {
		mCheckRunning = false;
		mCheckHandler.removeCallbacks(mCheckRunnable);
	}


	private void check() {
		if(System.currentTimeMillis() - mLastCommunicationTimestamp >= HB_PERIOD)
			sendHB();

		checkConnectionTimeout();
		checkMessagesTimeout();
	}


	private void sendHB() {
		sendMessage(Message.Nop::new);
	}


	private synchronized void checkMessagesTimeout() {
		Logcat.d("Messages status: PENDING=" + mPendingMessageList.size() + "; SENT=" + mSentMessageList.size() + "; SUBSCRIPTIONS=" +
				mSubscriptionCount + "; PENDING MUTATIONS=" + mPendingMutationCount + "; PENDING AUTH=" + mAuth.isAuthPending() +
				"; PENDING DEAUTH=" + mAuth.isDeauthPending());

		long now = System.currentTimeMillis();

		// message timeout
		for(int i = 0; i < mSentMessageList.size(); i++) {
			if(now - mSentMessageList.get(i).getSentTimestamp() > mConnectionTimeout) {
				timeoutMessage(mSentMessageList.remove(i));
				i--;
			} else {
				break;
			}
		}
		for(int i = 0; i < mPendingMessageList.size(); i++) {
			if(now - mPendingMessageList.get(i).getSentTimestamp() > mConnectionTimeout) {
				timeoutMessage(mPendingMessageList.remove(i));
				i--;
			} else {
				break;
			}
		}
	}


	private void checkConnectionTimeout() {
		long now = System.currentTimeMillis();

		// connection timeout
		if(mConnectionState == CONNECTING && now - mConnectionLossTimestamp > mConnectionTimeout) {
			for(MessageFuture mf : mSentMessageList) {
				RapidError error = new RapidError(TIMEOUT);
				mLogger.logE(error);
				mf.getRapidFuture().invokeError(error);
			}
			for(MessageFuture mf : mPendingMessageList) {
				RapidError error = new RapidError(TIMEOUT);
				mLogger.logE(error);
				mf.getRapidFuture().invokeError(error);
			}
			mSentMessageList.clear();
			mPendingMessageList.clear();
			mConnectionId = null;
			mSubscriptionCount = 0;
			mPendingMutationCount = 0;
			mCheckHandler.removeCallbacks(mConnectionRetryRunnable);
			stopCheckHandler();
			mCallback.onTimedOut();
			changeConnectionState(DISCONNECTED);
		}
	}


	private void sendAuthMessage() {
		try {
			Message.Auth m = new Message.Auth(mAuth.getAuthToken());
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.onSuccess(() -> mAuth.authSuccess());
			future.onError(error -> mAuth.authError(error));
			MessageFuture messageFuture = new MessageFuture(m, m.toJson().toString(), future);
			sendMessage(messageFuture);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}


	private void sendDeauthMessage() {
		try {
			Message.Deauth m = new Message.Deauth();
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.onSuccess(() -> mAuth.deauthSuccess());
			future.onError(error -> mAuth.deauthError());
			MessageFuture messageFuture = new MessageFuture(m, m.toJson().toString(), future);
			sendMessage(messageFuture);
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
}

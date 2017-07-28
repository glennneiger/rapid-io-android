package io.rapid;


import android.content.Context;
import android.os.Handler;

import com.annimon.stream.Stream;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.rapid.executor.RapidExecutor;

import static io.rapid.Config.CHECKER_HANDLER_PERIOD;
import static io.rapid.Config.HB_PERIOD;
import static io.rapid.ConnectionState.CONNECTED;
import static io.rapid.ConnectionState.CONNECTING;
import static io.rapid.ConnectionState.DISCONNECTED;
import static io.rapid.RapidError.ErrorType.SUBSCRIPTION_CANCELLED;
import static io.rapid.RapidError.ErrorType.TIMEOUT;


class WebSocketRapidConnection extends RapidConnection implements WebSocketConnection.WebSocketConnectionListener {
	private final Context mContext;
	private final RapidExecutor mExecutor;
	private final String mUrl;
	private final RapidLogger mLogger;
	private long mConnectionTimeout = Config.DEFAULT_CONNECTION_TIMEOUT;
	private WebSocketConnection mWebSocketConnection;
	private long mConnectionLossTimestamp = -1;
	private ConnectionState mConnectionState = DISCONNECTED;
	private String mConnectionId;
	private long mLastCommunicationTimestamp = 0;
	private int mSubscriptionCount = 0;
	private List<MessageFuture> mPendingMessageList = new ArrayList<>();
	private List<MessageFuture> mSentMessageList = new ArrayList<>();
	private AuthHelper mAuth;
	private boolean mCheckRunning = false;
	private Handler mCheckHandler = new Handler();
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();
	private Runnable mDisconnectRunnable = () -> disconnectWebSocketConnection(true);
	private Runnable mConnectionRetryRunnable = this::createWebSocketConnectionIfNeeded;
	private Runnable mCheckRunnable = () -> {
		startCheckHandler();
		check();
	};
	private Queue<RapidCallback.TimeOffset> mTimeOffsetCallbacks = new LinkedList<>();


	WebSocketRapidConnection(Context context, String url, Callback rapidConnectionCallback, RapidExecutor executor, RapidLogger logger) {
		super(rapidConnectionCallback);
		mContext = context;
		mUrl = url;
		mExecutor = executor;
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
		mAuth = new AuthHelper(mExecutor, authCallback, mLogger);
	}


	@Override
	public void onOpen() {
		changeConnectionState(CONNECTED);
		startCheckHandler();
		sendConnect();

		// send all pending (unsent) messages
		if(mAuth.isAuthPending() || mAuth.isAuthRequired()) {
			mAuth.setAuthPending();
			sendAuthMessage();
		}

		if(mAuth.isDeauthPending()) sendDeauthMessage();

		Stream.of(mPendingMessageList).forEach(this::sendMessage);
		mPendingMessageList.clear();
	}


	@Override
	public void onMessage(Message message) {
		mLastCommunicationTimestamp = System.currentTimeMillis();

		if(message.shouldBeAcked())
			sendMessage(() -> new Message.Ack(message.getEventId()));

		switch(message.getMessageType()) {
			case ERR:
				handleErrMessage((Message.Err) message);
				break;
			case CA:
				handleCaMessage((Message.Ca) message);
				break;
			case CA_CH:
				handleCaMessage((Message.CaCh) message);
				break;
			case ACK:
				handleAckMessage((Message.Ack) message);
				break;
			case VAL:
				Message.Val valMessage = ((Message.Val) message);
				mCallback.onValue(valMessage.getSubscriptionId(), valMessage.getCollectionId(), valMessage.getDocuments());
				break;
			case RES:
				Message.Res resMessage = ((Message.Res) message);
				handleResMessage(resMessage);
				mCallback.onFetchResult(resMessage.getFetchId(), resMessage.getCollectionId(), resMessage.getDocuments());
				break;
			case UPD:
				Message.Upd updMessage = ((Message.Upd) message);
				mCallback.onUpdate(updMessage.getSubscriptionId(), updMessage.getCollectionId(), updMessage.getDocument());
				break;
			case RM:
				Message.Rm rmMessage = ((Message.Rm) message);
				mCallback.onRemove(rmMessage.getSubscriptionId(), rmMessage.getCollectionId(), rmMessage.getDocument());
				break;
			case TS:
				Message.Ts tsMessage = (Message.Ts) message;
				handleTsMessage(tsMessage);
				break;
			case MES:
				Message.Mes mesMessage = (Message.Mes) message;
				mCallback.onChannelMessage(mesMessage.getSubscriptionId(), mesMessage.getChannelName(), mesMessage.getBody());
				break;
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
		listener.onConnectionStateChanged(mConnectionState);
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
		for(MessageFuture messageFuture : mPendingMessageList) {
			if(messageFuture.getMessage() instanceof Message.Sub && ((Message.Sub) messageFuture.getMessage())
					.getSubscriptionId().equals(subscription.getSubscriptionId())) {
				mPendingMessageList.remove(messageFuture);
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
		for(MessageFuture messageFuture : mPendingMessageList) {
			if(messageFuture.getMessage() instanceof Message.SubCh && ((Message.SubCh) messageFuture.getMessage())
					.getSubscriptionId().equals(subscription.getSubscriptionId())) {
				mPendingMessageList.remove(messageFuture);
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
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Mut(collectionName, documentJsonResolver.resolve()));
	}


	@Override
	public RapidFuture merge(String collectionName, FutureResolver<String> documentJsonResolver) {
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Mer(collectionName, documentJsonResolver.resolve()));
	}


	@Override
	public RapidFuture publish(String channelName, FutureResolver<String> messageJson) {
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Pub(channelName, messageJson.resolve()));
	}


	@Override
	public RapidFuture delete(String collectionName, FutureResolver<String> documentJsonResolver) {
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Del(collectionName, documentJsonResolver.resolve()));
	}


	@Override
	public void setConnectionTimeout(long connectionTimeoutMs) {
		mConnectionTimeout = connectionTimeoutMs;
	}


	@Override
	public RapidFuture getSetverTimeOffset(RapidCallback.TimeOffset callback) {
		createWebSocketConnectionIfNeeded();
		mTimeOffsetCallbacks.add(callback);
		return sendMessage(Message.ReqTs::new);
	}


	@Override
	public RapidActionFuture onDisconnectDelete(String collectionName, FutureResolver<String> documentJsonResolver) {
		createWebSocketConnectionIfNeeded();
		String actionId = IdProvider.getNewActionId();
		RapidActionFuture future = new RapidActionFuture(mExecutor, actionId, this);
		return (RapidActionFuture) sendMessage(future, () -> new Message.Da(actionId, new Message.Del(collectionName, documentJsonResolver.resolve())));
	}


	@Override
	public RapidActionFuture onDisconnectMutate(String collectionName, FutureResolver<String> documentJsonResolver) {
		createWebSocketConnectionIfNeeded();
		String actionId = IdProvider.getNewActionId();
		RapidActionFuture future = new RapidActionFuture(mExecutor, actionId, this);
		return (RapidActionFuture) sendMessage(future, () -> new Message.Da(actionId, new Message.Mut(collectionName, documentJsonResolver.resolve())));
	}


	@Override
	public RapidActionFuture onDisconnectMerge(String collectionName, FutureResolver<String> documentJsonResolver) {
		createWebSocketConnectionIfNeeded();
		String actionId = IdProvider.getNewActionId();
		RapidActionFuture future = new RapidActionFuture(mExecutor, actionId, this);
		return (RapidActionFuture) sendMessage(future, () -> new Message.Da(actionId, new Message.Mer(collectionName, documentJsonResolver.resolve())));
	}


	@Override
	public RapidFuture cancelOnDisconnect(String actionId) {
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.DaCa(actionId));
	}


	private void handleTsMessage(Message.Ts tsMessage) {
		long diff = System.currentTimeMillis() - tsMessage.getTimestamp();
		mTimeOffsetCallbacks.poll().onTimeOffsetReceived(diff);
	}


	private void handleResMessage(Message.Res resMessage) {
		mSubscriptionCount--;
	}


	private void createWebSocketConnectionIfNeeded() {
		if(!mPendingMessageList.isEmpty() || mSubscriptionCount > 0 || mAuth.isAuthPending() || mAuth.isDeauthPending()) {
			// cancel scheduled disconnect if any
			mCheckHandler.removeCallbacks(mDisconnectRunnable);
			if(mWebSocketConnection == null) {
				changeConnectionState(CONNECTING);
				mWebSocketConnection = new WebSocketConnectionAsync(mUrl, this, mExecutor);
				mWebSocketConnection.connectToServer(mContext);
			}
		}
	}


	private void disconnectWebSocketConnectionIfNeeded() {
		if(mSubscriptionCount == 0 && !mAuth.isAuthPending() && !mAuth.isDeauthPending()
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
		RapidFuture future = new RapidFuture(mExecutor);
		return sendMessage(future, message);
	}


	private RapidFuture sendMessage(RapidFuture baseFuture, FutureResolver<Message> message) {
		// send message in background
		mExecutor.doInBackground(() -> {
			MessageFuture messageFuture = createMessageFuture(message.resolve(), baseFuture);
			if(mConnectionState == CONNECTED) {
				sendMessage(messageFuture);
			} else {
				if(!(messageFuture.getMessage() instanceof Message.Nop)) mPendingMessageList.add(messageFuture);
			}
		});
		return baseFuture;
	}


	private MessageFuture createMessageFuture(Message message, RapidFuture future) {
		try {
			return new MessageFuture(message, message.toJson().toString(), future);
		} catch(JSONException e) {
			throw new IllegalArgumentException("Could not create message", e);
		}
	}


	private void sendMessage(MessageFuture msg) {
		if(msg.getMessage().shouldBeAcked())
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
		Stream.of(mConnectionStateListeners).filter(l -> l != null).forEach(l -> l.onConnectionStateChanged(state));
	}


	private void sendConnect() {
		boolean reconnect = true;
		if(mConnectionId == null) {
			mConnectionId = IdProvider.getConnectionId();
			reconnect = false;
		}
		boolean finalReconnect = reconnect;

		Message.Con m = new Message.Con(mConnectionId, finalReconnect);
		RapidFuture future = new RapidFuture(mExecutor);
		MessageFuture messageFuture = createMessageFuture(m, future);
		sendMessage(messageFuture);
	}


	private synchronized void handleErrMessage(Message.Err message) {
		switch(message.getType()) {
			case CONNECTION_TERMINATED:
				disconnectWebSocketConnection(false);
				mExecutor.doOnMain(() ->
				{
					createWebSocketConnectionIfNeeded();
					mSubscriptionCount = 0;
					reconnectSubscriptions();
				});
				break;
			default:
				MessageFuture sentMessage = Stream.of(mSentMessageList).filter(m -> message.getEventId().equals(m.getMessage().getEventId())).findFirst().orElse(null);
				if(sentMessage != null) {
					RapidError error = new RapidError(RapidError.ErrorType.fromServerError(message, sentMessage));
					mLogger.logE(error);
					sentMessage.getRapidFuture().invokeError(error);
					updateCountersOnError(sentMessage);
					mSentMessageList.remove(sentMessage);
				}

				break;
		}

		disconnectWebSocketConnectionIfNeeded();
	}


	private void updateCountersOnError(MessageFuture erroredMessageFuture) {
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
		MessageFuture sentMessage = Stream.of(mSentMessageList).filter(m -> ackMessage.getEventId().equals(m.getMessage().getEventId())).findFirst().orElse(null);

		if(sentMessage != null) {
			sentMessage.getRapidFuture().invokeSuccess();
			mSentMessageList.remove(sentMessage);
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
			sendMessage(Message.Nop::new);

		checkConnectionTimeout();
		checkMessagesTimeout();
	}


	private synchronized void checkMessagesTimeout() {
		Logcat.d("Messages status: \n---> PENDING=%d; SENT=%d; SUBSCRIPTIONS=%d; PENDING AUTH=%b; PENDING DEAUTH=%b", mPendingMessageList.size(), mSentMessageList.size(), mSubscriptionCount, mAuth.isAuthPending(), mAuth.isDeauthPending());

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
			mCheckHandler.removeCallbacks(mConnectionRetryRunnable);
			stopCheckHandler();
			mCallback.onTimedOut();
			changeConnectionState(DISCONNECTED);
		}
	}


	private void sendAuthMessage() {
		Message.Auth m = new Message.Auth(mAuth.getAuthToken());
		RapidFuture future = new RapidFuture(mExecutor);
		future.onSuccess(() -> mAuth.authSuccess());
		future.onError(error -> mAuth.authError(error));
		sendMessage(createMessageFuture(m, future));
	}


	private void sendDeauthMessage() {
		Message.Deauth m = new Message.Deauth();
		RapidFuture future = new RapidFuture(mExecutor);
		future.onSuccess(() -> mAuth.deauthSuccess());
		future.onError(error -> mAuth.deauthError());
		sendMessage(createMessageFuture(m, future));
	}
}

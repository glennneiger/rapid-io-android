package io.rapid;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static io.rapid.Config.CHECKER_HANDLER_PERIOD;
import static io.rapid.Config.HB_PERIOD;
import static io.rapid.ConnectionState.CONNECTED;
import static io.rapid.ConnectionState.CONNECTING;
import static io.rapid.ConnectionState.DISCONNECTED;
import static io.rapid.RapidError.ErrorType.SUBSCRIPTION_CANCELED;
import static io.rapid.RapidError.ErrorType.TIMEOUT;


class WebSocketRapidConnection extends RapidConnection implements WebSocketConnection.WebSocketConnectionListener
{

	private final Context mContext;
	private final Handler mOriginalThreadHandler;
	private final String mUrl;
	private WebSocketConnection mWebSocketConnection;
	private boolean mInternetConnected = true;
	private boolean mInternetConnectionBroadcast = false;
	private long mInternetLossTimestamp = -1;
	private ConnectionState mConnectionState = DISCONNECTED;
	private String mConnectionId;
	private long mLastCommunicationTimestamp = 0;
	private int mSubscriptionCount = 0;
	private int mPendingMutationCount = 0;
	private List<MessageFuture> mPendingMessageList = new ArrayList<>();
	private List<MessageFuture> mSentMessageList = new ArrayList<>();
	private boolean mPendingAuth;
	private String mAuthToken;
	private RapidFuture mAuthFuture;
	private boolean mAuthenticated = false;
	private boolean mCheckRunning = false;
	private Handler mCheckHandler = new Handler();
	private Runnable mCheckRunnable = () ->
	{
		startCheckHandler();
		check();
	};
	private Runnable mDisconnectRunnable = () -> {
		disconnectWebSocketConnection(true);
	};
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();


	private BroadcastReceiver mInternetConnectionBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
			{
				final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				final NetworkInfo info = connManager.getActiveNetworkInfo();
				boolean hasInternetConnection = info != null && info.isConnected();
				Logcat.d("Internet connected: " + hasInternetConnection);
				if(hasInternetConnection)
				{
					mInternetConnected = true;
					unregisterInternetConnectionBroadcast();
					createWebSocketConnectionIfNeeded();
				}
			}
		}
	};


	public WebSocketRapidConnection(Context context, String url, Callback rapidConnectionCallback, Handler originalThreadHandler)
	{
		super(rapidConnectionCallback);
		mContext = context;
		mUrl = url;
		mOriginalThreadHandler = originalThreadHandler;
	}


	@Override
	public void onOpen()
	{
		changeConnectionState(CONNECTED);
		startCheckHandler();
		sendConnect();

		if(!mPendingMessageList.isEmpty())
		{
			for(int i = 0; i < mPendingMessageList.size(); i++)
			{
				sendMessage(mPendingMessageList.get(i));
			}
			mPendingMessageList.clear();
		}
	}


	@Override
	public void onMessage(Message message)
	{
		mLastCommunicationTimestamp = System.currentTimeMillis();
		sendAckIfNeeded(message);

		if(message.getMessageType() == MessageType.ERR)
		{
			handleErrMessage((Message.Err) message);
		}
		else if(message.getMessageType() == MessageType.CA)
		{
			handleCaMessage((Message.Ca) message);
		}
		else if(message.getMessageType() == MessageType.ACK)
		{
			handleAckMessage((Message.Ack) message);
		}
		else if(message.getMessageType() == MessageType.VAL)
		{
			Message.Val valMessage = ((Message.Val) message);
			mCallback.onValue(valMessage.getSubscriptionId(), valMessage.getCollectionId(), valMessage.getDocuments());
		}
		else if(message.getMessageType() == MessageType.UPD)
		{
			Message.Upd updMessage = ((Message.Upd) message);
			mCallback.onUpdate(updMessage.getSubscriptionId(), updMessage.getCollectionId(), updMessage.getPreviousSiblingId(), updMessage.getDocument());
		}
	}


	@Override
	public void onClose(CloseReasonEnum reason)
	{
		if(reason != CloseReasonEnum.CLOSED_MANUALLY)
		{
			changeConnectionState(DISCONNECTED);
			mWebSocketConnection = null;
			startCheckHandler();
		}

		if(reason == CloseReasonEnum.INTERNET_CONNECTION_LOST ||
				reason == CloseReasonEnum.NO_INTERNET_CONNECTION)
		{
			mInternetConnected = false;
			mInternetLossTimestamp = System.currentTimeMillis();
			registerInternetConnectionBroadcast();
		}
	}


	@Override
	public void onError(Exception ex)
	{
		changeConnectionState(DISCONNECTED);
		mWebSocketConnection = null;
		stopCheckHandler();
		//TODO invoke error to user
	}


	@Override
	public RapidFuture authorize(String token)
	{
		if(token == null || !token.equals(mAuthToken))
		{
			mAuthToken = token;
			mPendingAuth = true;
			mAuthenticated = false;
			createWebSocketConnectionIfNeeded();
			mAuthFuture = sendMessage(() -> new Message.Auth(token));
		}
		else if(!mAuthenticated && !mPendingAuth)
		{
			mAuthenticated = false;
			createWebSocketConnectionIfNeeded();
			mAuthFuture = sendMessage(() -> new Message.Auth(token));
		}
		else if(mAuthenticated) {
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.invokeSuccess();
			return future;
		}

		return mAuthFuture;
	}


	@Override
	public void addConnectionStateListener(RapidConnectionStateListener listener)
	{
		mConnectionStateListeners.add(listener);
	}


	@Override
	public void removeConnectionStateListener(RapidConnectionStateListener listener)
	{
		mConnectionStateListeners.remove(listener);
	}


	@Override
	public void removeAllConnectionStateListeners()
	{
		mConnectionStateListeners.clear();
	}


	@Override
	public ConnectionState getConnectionState()
	{
		return mConnectionState;
	}


	@Override
	void subscribe(String subscriptionId, Subscription subscription)
	{
		Message.Sub messageSub = new Message.Sub(subscription.getCollectionName(), subscriptionId);
		messageSub.setFilter(subscription.getFilter());
		messageSub.setLimit(subscription.getLimit());
		messageSub.setOrder(subscription.getOrder());
		messageSub.setSkip(subscription.getSkip());

		mSubscriptionCount++;
		createWebSocketConnectionIfNeeded();
		RapidFuture future = sendMessage(() -> messageSub);
		future.onError(error -> mCallback.onError(subscriptionId, subscription.getCollectionName(), error));
	}


	@Override
	public void onUnsubscribe(Subscription subscription)
	{
		boolean sendUnsubscribe = true;
		for(int i = mPendingMessageList.size() - 1; i >= 0; i--)
		{
			if(mPendingMessageList.get(i).getMessage() instanceof Message.Sub && ((Message.Sub) mPendingMessageList.get(i).getMessage())
					.getSubscriptionId().equals(subscription.getSubscriptionId()))
			{
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
	public RapidFuture mutate(String collectionName, FutureResolver<String> documentJsonResolver)
	{
		mPendingMutationCount++;
		createWebSocketConnectionIfNeeded();
		return sendMessage(() -> new Message.Mut(collectionName, documentJsonResolver.resolve()));
	}


	private void authorizeIfNeeded()
	{
		if(mAuthToken != null && !mPendingAuth)
		{
			sendMessage(() -> new Message.Auth(mAuthToken));
		}
	}


	private void createWebSocketConnectionIfNeeded() {
		if(mPendingMutationCount > 0 || mSubscriptionCount > 0 || mPendingAuth) {
			// cancel scheduled disconnect if any
			mCheckHandler.removeCallbacks(mDisconnectRunnable);
			if(mWebSocketConnection == null) {
				authorizeIfNeeded();
				if(mInternetConnected) {
					changeConnectionState(CONNECTING);
					mWebSocketConnection = new WebSocketConnectionAsync(mUrl, this);
					mWebSocketConnection.connectToServer();
				} else {
					mInternetLossTimestamp = System.currentTimeMillis();
					registerInternetConnectionBroadcast();
					if(!mCheckRunning) {
						startCheckHandler();
					}
				}
			}
		}
	}


	private void disconnectWebSocketConnectionIfNeeded() {
		if(mSubscriptionCount == 0 && mPendingMutationCount == 0 && !mPendingAuth) {
			Logcat.d("Scheduling websocket disconnection in %d ms", Config.WEBSOCKET_DISCONNECT_TIMEOUT);
			mCheckHandler.postDelayed(mDisconnectRunnable, Config.WEBSOCKET_DISCONNECT_TIMEOUT);
		}
	}


	private void disconnectWebSocketConnection(boolean sendDisconnectMessage)
	{
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


	private void registerInternetConnectionBroadcast()
	{
		if(mContext != null && !mInternetConnectionBroadcast)
		{
			mInternetConnectionBroadcast = true;
			mContext.registerReceiver(mInternetConnectionBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
	}


	private void unregisterInternetConnectionBroadcast()
	{
		if(mContext != null && mInternetConnectionBroadcast)
		{
			mInternetConnectionBroadcast = false;
			mContext.unregisterReceiver(mInternetConnectionBroadcastReceiver);
		}
	}


	private void reconnectSubscriptions() {
		getCallback().onReconnected();
	}


	private RapidFuture sendMessage(FutureResolver<Message> message) {
		RapidFuture future = new RapidFuture(mOriginalThreadHandler);

		// send message in background
		new AsyncTask<Void, Void, MessageFuture>() {
			@Override
			protected MessageFuture doInBackground(Void... params) {
				Message m = message.resolve();
				try {
					MessageFuture messageFuture = new MessageFuture(m, m.toJson().toString(), future);
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
		return future;
	}


	private void sendMessage(MessageFuture msg) {
		if(msg.getMessage().getMessageType() != MessageType.ACK && msg.getMessage().getMessageType() != MessageType.NOP)
			mSentMessageList.add(msg);
		mWebSocketConnection.sendMessage(msg.getMessageJson());
		mLastCommunicationTimestamp = System.currentTimeMillis();
	}


	private void changeConnectionState(ConnectionState state) {
		Logcat.d(state.name());
		if(mConnectionState != state)
		{
			mConnectionState = state;
			invokeConnectionStateChanged(state);
		}
	}


	private void invokeConnectionStateChanged(ConnectionState state)
	{
		for(RapidConnectionStateListener l : mConnectionStateListeners)
		{
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

		try
		{
			Message.Con m = new Message.Con(mConnectionId, finalReconnect);
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			MessageFuture messageFuture = new MessageFuture(m, m.toJson().toString(), future);
			sendMessage(messageFuture);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}


	private void sendAckIfNeeded(Message parsedMessage) {
		if(parsedMessage.getMessageType() == MessageType.VAL || parsedMessage.getMessageType() == MessageType.UPD) {
			sendMessage(() -> new Message.Ack(parsedMessage.getEventId()));
		}
	}


	private synchronized void handleErrMessage(Message.Err message) {
		switch(message.getErrorType())
		{
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
					messageFuture.getRapidFuture().invokeError(new RapidError(message));
					if(messageFuture.getMessage() instanceof Message.Mut) mPendingMutationCount--;
					if(messageFuture.getMessage() instanceof Message.Auth) mPendingAuth = false;
					mSentMessageList.remove(position);
				}

				break;
		}
	}


	private synchronized void handleCaMessage(Message.Ca message) {
		getCallback().onError(message.getSubscriptionId(), message.getCollectionId(), new RapidError(SUBSCRIPTION_CANCELED));
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
			if(messageFuture.getMessage() instanceof Message.Mut) mPendingMutationCount--;
			if(messageFuture.getMessage() instanceof Message.Auth)
			{
				mPendingAuth = false;
				mAuthenticated = true;
			}
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
		sendMessage(() -> new Message.Nop());
	}


	private synchronized void checkMessagesTimeout() {
		Logcat.d("Pending list: " + mPendingMessageList.size() + "; Sent list: " + mSentMessageList.size() + "; Subscription count: " +
				mSubscriptionCount + "; Pending mutation count: " + mPendingMutationCount + "; Pending auth: " + mPendingAuth);

		long now = System.currentTimeMillis();

		// message timeout
		for(int i = mSentMessageList.size() - 1; i >= 0; i--)
		{
			if(now - mSentMessageList.get(0).getSentTimestamp() > Config.MESSAGE_TIMEOUT) {
				timeoutMessage(mPendingMessageList.remove(0));
			}
		}
		for(int i = mPendingMessageList.size() - 1; i >= 0; i--)
		{
			if(now - mPendingMessageList.get(0).getSentTimestamp() > Config.MESSAGE_TIMEOUT) {
				timeoutMessage(mPendingMessageList.remove(0));
			}
		}
	}


	private void timeoutMessage(MessageFuture message) {
		if(message.getMessage() instanceof Message.Sub) mSubscriptionCount--;
		if(message.getMessage() instanceof Message.Mut) mPendingMutationCount--;
		if(message.getMessage() instanceof Message.Auth) mPendingAuth = false;
		message.getRapidFuture().invokeError(new RapidError(TIMEOUT));
	}


	private void checkConnectionTimeout() {
		long now = System.currentTimeMillis();

		// connection timeout
		if(!mInternetConnected && now - mInternetLossTimestamp > Config.CONNECTION_TIMEOUT) {
			for(MessageFuture mf : mSentMessageList) {
				mf.getRapidFuture().invokeError(new RapidError(TIMEOUT));
			}
			for(MessageFuture mf : mPendingMessageList) {
				mf.getRapidFuture().invokeError(new RapidError(TIMEOUT));
			}
			mSentMessageList.clear();
			mPendingMessageList.clear();
			mConnectionId = null;
			mSubscriptionCount = 0;
			mPendingMutationCount = 0;
			mPendingAuth = false;
			stopCheckHandler();
			unregisterInternetConnectionBroadcast();
			mCallback.onTimedOut();
		}
	}



}

package io.rapid;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.google.gson.Gson;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rapid.converter.RapidGsonConverter;
import io.rapid.converter.RapidJsonConverter;


public class Rapid implements WebSocketConnection.WebSocketConnectionListener {
	private static Map<String, Rapid> sInstances = new HashMap<>();
	private final Context mContext;
	private final String mApiKey;
	private RapidJsonConverter mJsonConverter;
	private WebSocketConnection mWebSocketConnection;
	private String mConnectionId;
	private Handler mHandler = new Handler();
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();
	private boolean mInternetConnected = true;

	private CollectionProvider mCollectionProvider;
	private Map<String, MessageFuture> mPendingMessages = new HashMap<>();


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
				if(hasInternetConnection) {
					unregisterInternetConnectionBroadcast();
					createNewWebSocketConnection();
					mInternetConnected = true;
				}
			}
		}

	};


	private Rapid(Context context, String apiKey) {
		mContext = context;
		mApiKey = apiKey;
		mJsonConverter = new RapidGsonConverter(new Gson());

		mCollectionProvider = new InMemoryCollectionProvider();
	}


	public static Rapid getInstance(String apiKey) {
		if(!sInstances.containsKey(apiKey))
			throw new IllegalStateException("Rapid SDK not initialized. Please call Rapid.initialize(apiKey) first.");
		return sInstances.get(apiKey);
	}


	public static Rapid getInstance() {
		if(sInstances.isEmpty())
			throw new IllegalStateException("Rapid SDK not initialized. Please call Rapid.initialize(apiKey) first.");
		else if(sInstances.size() > 1) {
			throw new IllegalStateException("Multiple Rapid instances initialized. Please use Rapid.getInstance(apiKey) to select the one you need.");
		} else {
			return getInstance(sInstances.keySet().iterator().next());
		}
	}


	public static void initialize(Application context, String apiKey) {
		if(!sInstances.containsKey(apiKey))
			sInstances.put(apiKey, new Rapid(context, apiKey));
	}


	@Override
	public void onOpen() {
	}


	@Override
	public void onMessage(MessageBase message) {
		if (message.getMessageType()== MessageBase.MessageType.ACK){
			MessageAck ackMessage = ((MessageAck) message);
			MessageFuture messageFuture = mPendingMessages.remove(ackMessage.getEventId());
			messageFuture.invokeSuccess();
		} else if(message.getMessageType() == MessageBase.MessageType.VAL) {
			MessageVal valMessage = ((MessageVal) message);
			mCollectionProvider.findCollectionByName(valMessage.getCollectionId()).onValue(valMessage);
		} else if(message.getMessageType() == MessageBase.MessageType.UPD)
		{
			MessageUpd updMessage = ((MessageUpd) message);
			mCollectionProvider.findCollectionByName(updMessage.getCollectionId()).onUpdate(updMessage);
		} else if(message.getMessageType() == MessageBase.MessageType.ERR)
		{
			switch(((MessageErr)message).getErrorType())
			{
				case CONNECTION_TERMINATED:
					getHandler().post(() ->
					{
						disconnectFromServer();
						createNewWebSocketConnection();
						reconnectSubscriptions();
					});
					break;
			}
		}
	}


	@Override
	public void onClose(WebSocketConnection.CloseReasonEnum reason) {
		if(reason == WebSocketConnection.CloseReasonEnum.INTERNET_CONNECTION_LOST ||
				reason == WebSocketConnection.CloseReasonEnum.NO_INTERNET_CONNECTION)
		{
			mInternetConnected = false;
			registerInternetConnectionBroadcast();
		}
	}


	private void registerInternetConnectionBroadcast()
	{
		if(mContext != null) mContext.registerReceiver(mInternetConnectionBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}


	private void unregisterInternetConnectionBroadcast()
	{
		if(mContext != null) mContext.unregisterReceiver(mInternetConnectionBroadcastReceiver);
	}


	@Override
	public void onError(Exception ex) {

	}


	@Override
	public void onConnectionStateChange(ConnectionState state)
	{
		invokeConnectionStateChanged(state);
	}


	public <T> RapidCollectionReference<T> collection(String collectionName, Class<T> itemClass) {
		return mCollectionProvider.provideCollection(this, collectionName, itemClass);
	}


	public RapidJsonConverter getJsonConverter() {
		return mJsonConverter;
	}


	public void setJsonConverter(RapidJsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
	}


	public String getApiKey() {
		return mApiKey;
	}


	void onSubscribe(Subscription subscription){
		// some subscription subscribed - connect if not connected
		if(mInternetConnected && (mWebSocketConnection == null || mWebSocketConnection.getConnectionState() == ConnectionState.CLOSED)) {
			createNewWebSocketConnection();
		}
	}


	void onUnsubscribe(Subscription subscription) {
		// some subscription unsubscribed - check if we have any more subscriptions and disconnect if not
		boolean subscribed = false;
		for(RapidCollectionReference rapidCollectionReference : mCollectionProvider.getCollections().values()) {
			if (rapidCollectionReference.isSubscribed()){
				subscribed = true;
				break;
			}
		}

		if (!subscribed){
			disconnectFromServer();
		}
	}


	Handler getHandler() {
		return mHandler;
	}


	MessageFuture sendMessage(MessageBase message) {
		MessageFuture future = new MessageFuture();
		mPendingMessages.put(message.getEventId(), future);
		mWebSocketConnection.sendMessage(message);
		return future;
	}


	public void addConnectionStateListener(RapidConnectionStateListener mListener)
	{
		mConnectionStateListeners.add(mListener);
	}


	public void removeConnectionStateListener(RapidConnectionStateListener mListener)
	{
		mConnectionStateListeners.remove(mListener);
	}


	public void removeAllConnectionStateListeners()
	{
		mConnectionStateListeners.clear();
	}


	public ConnectionState getConnectionState() {
		return mWebSocketConnection.getConnectionState();
	}


	private void invokeConnectionStateChanged(ConnectionState state) {
		for(RapidConnectionStateListener l : mConnectionStateListeners)
		{
			if(l != null) l.onConnectionStateChanged(state);
		}
	}


	private void disconnectFromServer()
	{
		mWebSocketConnection.disconnectFromServer(false);
		mConnectionId = null;
	}


	private void createNewWebSocketConnection()
	{
		if(mConnectionId == null) mConnectionId = IdProvider.getConnectionId();
		mWebSocketConnection = new WebSocketConnection(mConnectionId, URI.create(Config.URI), this);
		mWebSocketConnection.connectToServer();
	}


	private void reconnectSubscriptions()
	{
		for(RapidCollectionReference rapidCollectionReference : mCollectionProvider.getCollections().values())
		{
			if(rapidCollectionReference.isSubscribed())
			{
				rapidCollectionReference.resubscribe();
			}
		}
	}
}

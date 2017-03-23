package io.rapid;


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
	private final String mApiKey;
	private RapidJsonConverter mJsonConverter;
	private WebSocketConnection mWebSocketConnection;
	private Handler mHandler = new Handler();
	private List<RapidConnectionStateListener> mConnectionStateListeners = new ArrayList<>();

	private CollectionProvider mCollectionProvider;
	private Map<String, MessageFuture> mPendingMessages = new HashMap<>();


	private Rapid(String apiKey) {
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


	public static void initialize(String apiKey) {
		if(!sInstances.containsKey(apiKey))
			sInstances.put(apiKey, new Rapid(apiKey));
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
		}
	}


	@Override
	public void onClose(WebSocketConnection.CloseReasonEnum reason) {

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


	void onSubscribe(RapidSubscription subscription){
		// some subscription subscribed - connect if not connected
		mWebSocketConnection = new WebSocketConnection(URI.create(Config.URI), this);
		mWebSocketConnection.connectToServer();

	}


	void onUnsubscribe(RapidSubscription subscription) {
		// some subscription unsubscribed - check if we have any more subscriptions and disconnect if not
		boolean subscribed = false;
		for(RapidCollectionReference rapidCollectionReference : mCollectionProvider.getCollections().values()) {
			if (rapidCollectionReference.isSubscribed()){
				subscribed = true;
				break;
			}
		}

		if (!subscribed){
			mWebSocketConnection.disconnectFromServer();
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
}

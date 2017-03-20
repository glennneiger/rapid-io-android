package io.rapid;


import android.os.Handler;

import com.google.gson.Gson;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import io.rapid.converter.RapidGsonConverter;
import io.rapid.converter.RapidJsonConverter;


public class Rapid implements WebSocketConnection.WebSocketConnectionListener {
	private static Map<String, Rapid> sInstances = new HashMap<>();
	private final String mApiKey;
	private RapidJsonConverter mJsonConverter;
	private WebSocketConnection mWebSocketConnection;
	private Handler mHandler = new Handler();

	private CollectionProvider mCollectionProvider;


	private Rapid(String apiKey) {
		mApiKey = apiKey;
		mJsonConverter = new RapidGsonConverter(new Gson());
		mWebSocketConnection = new WebSocketConnection(URI.create(Config.URI), this);
		mWebSocketConnection.connectToServer();

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
		sInstances.put(apiKey, new Rapid(apiKey));
	}


	@Override
	public void onOpen() {

	}


	@Override
	public void onMessage(MessageBase message) {
		if(message.getMessageType() == MessageBase.MessageType.VAL) {
			MessageVal valMessage = ((MessageVal) message);
			mCollectionProvider.findCollectionByName(valMessage.getCollectionId()).onValue(valMessage);
		} else if(message.getMessageType() == MessageBase.MessageType.UPD) {
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


	public Handler getHandler() {
		return mHandler;
	}


	public ConnectionState getConnectionState() {
		return mWebSocketConnection.getConnectionState();
	}


	public void sendMessage(MessageBase message) {
		mWebSocketConnection.sendMessage(message);
	}
}

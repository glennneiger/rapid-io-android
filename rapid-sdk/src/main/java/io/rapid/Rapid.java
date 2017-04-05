package io.rapid;


import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import io.rapid.converter.RapidGsonConverter;
import io.rapid.converter.RapidJsonConverter;


public class Rapid {
	private static Map<String, Rapid> sInstances = new HashMap<>();
	private final String mApiKey;
	private RapidJsonConverter mJsonConverter;
	private Handler mHandler;
	private RapidConnection mRapidConnection;
	private CollectionProvider mCollectionProvider;


	private Rapid(Context context, String apiKey) {
		mApiKey = apiKey;
		mJsonConverter = new RapidGsonConverter(new Gson());
		mHandler = new Handler();
		mRapidConnection = new WebSocketRapidConnection(context, mHandler, new RapidConnection.Callback() {
			@Override
			public void onValue(String subscriptionId, String collectionId, String documentsJson) {
				mCollectionProvider.findCollectionByName(collectionId).onValue(subscriptionId, documentsJson);
			}


			@Override
			public void onUpdate(String subscriptionId, String collectionId, String previousSiblingId, String documentJson) {
				mCollectionProvider.findCollectionByName(collectionId).onUpdate(subscriptionId, previousSiblingId, documentJson);
			}


			@Override
			public void onReconnected() {
				mCollectionProvider.resubscribeAll();
			}
		});
		mCollectionProvider = new CollectionProvider(mRapidConnection, mJsonConverter, mHandler);
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


	public <T> RapidCollectionReference<T> collection(String collectionName, Class<T> itemClass) {
		return mCollectionProvider.provideCollection(collectionName, itemClass);
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


	public void addConnectionStateListener(RapidConnectionStateListener listener) {
		mRapidConnection.addConnectionStateListener(listener);
	}


	public void removeConnectionStateListener(RapidConnectionStateListener listener) {
		mRapidConnection.removeConnectionStateListener(listener);
	}


	public void removeAllConnectionStateListeners() {
		mRapidConnection.removeAllConnectionStateListeners();
	}


	public ConnectionState getConnectionState() {
		return mRapidConnection.getConnectionState();
	}
}

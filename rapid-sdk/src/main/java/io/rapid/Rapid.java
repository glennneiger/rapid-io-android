package io.rapid;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rapid.converter.RapidGsonConverter;
import io.rapid.converter.RapidJsonConverter;


public class Rapid {
	private static Map<String, Rapid> sInstances = new HashMap<>();
	private static Context sApplicationContext;
	private final String mApiKey;
	private RapidJsonConverter mJsonConverter;
	private Handler mHandler;
	private RapidConnection mRapidConnection;
	private CollectionProvider mCollectionProvider;


	private Rapid(Context context, String apiKey) {
		mApiKey = apiKey;
		mJsonConverter = new RapidGsonConverter(new Gson());
		mHandler = new Handler();

		String url = "ws://" + new String(Base64.decode(mApiKey, Base64.DEFAULT));

		mRapidConnection = new WebSocketRapidConnection(context, url, new RapidConnection.Callback() {
			@Override
			public void onValue(String subscriptionId, String collectionId, String documentsJson) {
				mCollectionProvider.findCollectionByName(collectionId).onValue(subscriptionId, documentsJson);
			}


			@Override
			public void onUpdate(String subscriptionId, String collectionId, String previousSiblingId, String documentJson) {
				mCollectionProvider.findCollectionByName(collectionId).onUpdate(subscriptionId, previousSiblingId, documentJson);
			}


			@Override
			public void onError(String subscriptionId, String collectionId, RapidError error) {
				mCollectionProvider.findCollectionByName(collectionId).onError(subscriptionId, error);
			}


			@Override
			public void onCancel(String subscriptionId, String collectionId)
			{

			}


			@Override
			public void onTimedOut() {
				mCollectionProvider.timedOutAll();
			}


			@Override
			public void onReconnected() {
				mCollectionProvider.resubscribeAll();
			}
		}, mHandler);

		SubscriptionDiskCache subscriptionDiskCache;
		try {
			subscriptionDiskCache = new SubscriptionDiskCache(context, mApiKey, Config.CACHE_DEFAULT_SIZE_MB);
		} catch(IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Subscription cache could not be initialized");
		}

		mCollectionProvider = new CollectionProvider(mRapidConnection, mJsonConverter, mHandler, subscriptionDiskCache);
	}


	public static Rapid getInstance(String apiKey) {
		if(!sInstances.containsKey(apiKey))
			initialize(apiKey);
		return sInstances.get(apiKey);
	}


	public static Rapid getInstance() {
		if(sInstances.isEmpty())
			throw new IllegalStateException("Rapid SDK not initialized. Please call Rapid.initialize(apiKey) first or add API key to AndroidManifest.xml.");
		else if(sInstances.size() > 1) {
			throw new IllegalStateException("Multiple Rapid instances initialized. Please use Rapid.getInstance(apiKey) to select the one you need.");
		} else {
			return getInstance(sInstances.keySet().iterator().next());
		}
	}


	public static void initialize(String apiKey) {
		Logcat.d("Initializing Rapid.io with API key: %s", apiKey);
		if(!sInstances.containsKey(apiKey))
			sInstances.put(apiKey, new Rapid(sApplicationContext, apiKey));
	}


	static void injectContext(Context context) {
		sApplicationContext = context.getApplicationContext();

		// try to auto-init from AndroidManifest metadata
		try {
			ApplicationInfo app = sApplicationContext.getPackageManager().getApplicationInfo(sApplicationContext.getPackageName(), PackageManager.GET_META_DATA);
			Bundle metaData = app.metaData;
			if(metaData != null) {
				String apiKey = metaData.getString(Config.API_KEY_METADATA);
				if(apiKey != null) {
					initialize(apiKey);
				}
			}

		} catch(PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
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


	public void setCachingEnabled(boolean cachingEnabled) {
		mCollectionProvider.getSubscriptionDiskCache().setEnabled(cachingEnabled);
	}


	public void setCacheSize(int cacheSizeInMb) {
		mCollectionProvider.getSubscriptionDiskCache().setMaxSize(cacheSizeInMb);
	}
}

package io.rapid;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

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
	private RapidConnection mRapidConnection;
	private CollectionProvider mCollectionProvider;
	private RapidLogger mLogger = new RapidLogger();


	private Rapid(Context context, String apiKey) {
		setLogLevel(LogLevel.LOG_LEVEL_INFO);
		mLogger.logI("Initializing Rapid.io SDK with API key: %s", apiKey);

		mApiKey = apiKey;
		mJsonConverter = new RapidGsonConverter();
		Handler handler = new Handler();

		AppMetadata appMetadata = new AppMetadata(apiKey);
		Logcat.d("URL: " + appMetadata.getUrl());

		mRapidConnection = new WebSocketRapidConnection(context, appMetadata.getUrl(), new RapidConnection.Callback() {
			@Override
			public void onValue(String subscriptionId, String collectionId, String documentsJson) {
				mCollectionProvider.findCollectionByName(collectionId).onValue(subscriptionId, documentsJson);
			}


			@Override
			public void onUpdate(String subscriptionId, String collectionId, String documentJson) {
				mCollectionProvider.findCollectionByName(collectionId).onUpdate(subscriptionId, documentJson);
			}


			@Override
			public void onError(String subscriptionId, String collectionId, RapidError error) {
				mCollectionProvider.findCollectionByName(collectionId).onError(subscriptionId, error);
			}


			@Override
			public void onRemove(String subscriptionId, String collectionId, String documentJson) {
				mCollectionProvider.findCollectionByName(collectionId).onRemove(subscriptionId, documentJson);
			}


			@Override
			public void onTimedOut() {
				mCollectionProvider.timedOutAll();
			}


			@Override
			public void onReconnected() {
				mCollectionProvider.resubscribeAll();
			}
		}, handler, mLogger);

		SubscriptionDiskCache subscriptionDiskCache;
		try {
			subscriptionDiskCache = new SubscriptionDiskCache(context, mApiKey, Config.CACHE_DEFAULT_SIZE_MB);
		} catch(IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Subscription cache could not be initialized");
		}

		mCollectionProvider = new CollectionProvider(mRapidConnection, mJsonConverter, handler, subscriptionDiskCache, mLogger);
	}


	/**
	 *
	 * @param apiKey RapidIO API key. You can get it in the developers console
	 * @return instance of Rapid
	 */
	public static Rapid getInstance(String apiKey) {
		if(!sInstances.containsKey(apiKey))
			initialize(apiKey);
		return sInstances.get(apiKey);
	}


	/**
	 *
	 * @return instance of Rapid
	 */
	public static Rapid getInstance() {
		if(sInstances.isEmpty())
			throw new IllegalStateException("Rapid SDK not initialized. Please call Rapid.initialize(apiKey) first or add API key to AndroidManifest.xml.");
		else if(sInstances.size() > 1) {
			throw new IllegalStateException("Multiple Rapid instances initialized. Please use Rapid.getInstance(apiKey) to select the one you need.");
		} else {
			return getInstance(sInstances.keySet().iterator().next());
		}
	}


	/**
	 *
	 * @param apiKey RapidIO API key. You can get it in the developers console
	 */
	public static void initialize(String apiKey) {
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


	/**
	 *
	 * @param collectionName collection name
	 * @param itemClass the class of T
	 * @param <T> the type of the desired object
	 * @return collection reference
	 */
	public <T> RapidCollectionReference<T> collection(String collectionName, Class<T> itemClass) {
		return mCollectionProvider.provideCollection(collectionName, itemClass);
	}


	/**
	 *
	 * @param collectionName collection name
	 * @return collection reference
	 */
	public RapidCollectionReference<Map<String, Object>> collection(String collectionName) {
		return mCollectionProvider.provideCollection(collectionName);
	}


	/**
	 * Method for user authorization. Data from collection are readable/writable/editable based on rules and authorization token.
	 * @param token authorization token
	 * @return RapidFuture. You can set success or error callback on it.
	 */
	public RapidFuture authorize(String token) {

		return mRapidConnection.authorize(token);
	}


	/**
	 * Method for canceling authorization.
	 * @return RapidFuture. You can set success or error callback on it.
	 */
	public RapidFuture deauthorize() {

		return mRapidConnection.deauthorize();
	}


	/**
	 * Rapid JSON converter. Converter is used for serialization and deserialization objects.
	 * @return RapidJsonConverter
	 */
	public RapidJsonConverter getJsonConverter() {
		return mJsonConverter;
	}


	/**
	 * Method for changing JSON converter.
	 * @param jsonConverter Custom JSON converter
	 */
	public void setJsonConverter(RapidJsonConverter jsonConverter) {
		mJsonConverter = jsonConverter;
	}


	/**
	 *
	 * @return RapidIO API key
	 */
	public String getApiKey() {
		return mApiKey;
	}


	/**
	 * Method for setting connection state listener. Listener gives you information about Rapid connection state.
	 * @param listener RapidConnectionStateListener
	 */
	public void addConnectionStateListener(RapidConnectionStateListener listener) {
		mRapidConnection.addConnectionStateListener(listener);
	}


	/**
	 * Method for removing connection state listener.
	 * @param listener RapidConnectionStateListener
	 */
	public void removeConnectionStateListener(RapidConnectionStateListener listener) {
		mRapidConnection.removeConnectionStateListener(listener);
	}


	/**
	 * Method for removing all connection state listeners
	 */
	public void removeAllConnectionStateListeners() {
		mRapidConnection.removeAllConnectionStateListeners();
	}


	/**
	 * Method for getting Rapid connection state.
	 * @return ConnectionState
	 */
	public ConnectionState getConnectionState() {
		return mRapidConnection.getConnectionState();
	}


	/**
	 * Method for enabling/disabling subscriptions cache
	 * @param cachingEnabled
	 */
	public void setCachingEnabled(boolean cachingEnabled) {
		mCollectionProvider.getSubscriptionDiskCache().setEnabled(cachingEnabled);
	}


	/**
	 * Method for changing cache size. Default size is 50 MB.
	 * @param cacheSizeInMb Cache size in MB
	 */
	public void setCacheSize(int cacheSizeInMb) {
		mCollectionProvider.getSubscriptionDiskCache().setMaxSize(cacheSizeInMb);
	}


	/**
	 * Method for setting Log level.
	 * @param level Log level
	 */
	public void setLogLevel(@LogLevel int level) {
		mLogger.setLevel(level);
	}


	/**
	 * Method for getting information if Rapid is authenticated.
	 * @return
	 */
	public boolean isAuthenticated() {
		return mRapidConnection.isAuthenticated();
	}
}

package io.rapid;


import java.util.HashMap;
import java.util.Map;

import io.rapid.converter.RapidGsonConverter;
import io.rapid.converter.RapidJsonConverter;


public class Rapid {
	private static Map<String, Rapid> sInstances = new HashMap<>();
	private static CacheProvider sCacheProvider;
	private static RapidExecutor sExecutor;
	private final String mApiKey;
	private JsonConverterProvider mJsonConverter;
	private RapidConnection mRapidConnection;
	private CollectionProvider mCollectionProvider;
	private static RapidLogger sLogger;


	private Rapid(String apiKey, DiskCache diskCache, RapidExecutor executor) {
		setLogLevel(LogLevel.LOG_LEVEL_INFO);

		sLogger.logI("Initializing Rapid.io SDK with API key: %s", apiKey);

		mApiKey = apiKey;
		mJsonConverter = new JsonConverterProvider(new RapidGsonConverter());

		AppMetadata appMetadata = new AppMetadata(apiKey);
		Logcat.d("URL: " + appMetadata.getUrl());

		mRapidConnection = new WebSocketRapidConnection(appMetadata.getUrl(), new RapidConnection.Callback() {
			@Override
			public void onValue(String subscriptionId, String collectionId, String documentsJson) {
				mCollectionProvider.findCollectionByName(collectionId).onValue(subscriptionId, documentsJson);
			}


			@Override
			public void onFetchResult(String fetchId, String collectionId, String documentsJson) {
				mCollectionProvider.findCollectionByName(collectionId).onFetchResult(fetchId, documentsJson);
			}


			@Override
			public void onUpdate(String subscriptionId, String collectionId, String documentJson) {
				mCollectionProvider.findCollectionByName(collectionId).onUpdate(subscriptionId, documentJson);
			}


			@Override
			public void onCollectionError(String subscriptionId, String collectionId, RapidError error) {
				mCollectionProvider.findCollectionByName(collectionId).onError(subscriptionId, error);
			}


			@Override
			public void onChannelError(String subscriptionId, String channelId, RapidError error) {
				mCollectionProvider.findChannelBySubscriptionId(subscriptionId).onError(subscriptionId, error);
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


			@Override
			public void onChannelMessage(String subscriptionId, String channelName, String body) {
				mCollectionProvider.findChannelBySubscriptionId(subscriptionId).onMessage(subscriptionId, channelName, body);
			}
		}, executor, sLogger);

		mCollectionProvider = new CollectionProvider(mRapidConnection, mJsonConverter, executor, new SubscriptionDiskCache(diskCache), sLogger, sCacheProvider);
	}


	/**
	 * Use this method to obtain specific {@link Rapid} instance
	 * in case you have initialized more than one instance in a single app
	 * <p>
	 * If you have initialized only one instance of {@link Rapid}, you can use {@link #getInstance()} to obtain the instance
	 *
	 * @param apiKey Rapid.io API key (you can get it in the developer's console)
	 * @return instance of {@link Rapid}
	 */
	public static Rapid getInstance(String apiKey) {
		if(!sInstances.containsKey(apiKey))
			initialize(apiKey);
		return sInstances.get(apiKey);
	}


	/**
	 * Use this method to obtain default {@link Rapid} instance initialized by calling {@link Rapid#initialize(String)}
	 * <p>
	 * If you have initialized multiple {@link Rapid} instances, use {@link Rapid#getInstance(String)} to obtain the right instance
	 *
	 * @return instance of {@link Rapid}
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
	 * Initialize {@link Rapid} instance with an Rapid.io API key
	 * <p>
	 * You can substitute this call by including API key in AndroidManifest file within {@code <application>} tag
	 * <p>
	 * Example:
	 * <pre>
	 * {@code
	 * <manifest>
	 * 	<application>
	 * 		//...
	 * 		<meta-data
	 * 			android:name="io.rapid.apikey"
	 * 			android:value="<API_KEY>" />
	 * 	</application>
	 * </manifest>
	 * }
	 * </pre>
	 *
	 * @param apiKey Rapid.io API key (you can get it in the developer's console)
	 */
	public static void initialize(String apiKey) {


		if(!sInstances.containsKey(apiKey))
			sInstances.put(apiKey, new Rapid(apiKey, sCacheProvider.getNewDiskCache(apiKey), sExecutor));
	}


	public static void setCacheProvider(CacheProvider cacheProvider) {
		sCacheProvider = cacheProvider;
	}


	public static void setExecutor(RapidExecutor executor) {
		sExecutor = executor;
	}


	public static void setLoggerOutput(LoggerOutput output) {
		sLogger = new RapidLogger(output);
	}


	/**
	 * Get collection reference {@link RapidCollectionReference} to create subscriptions and/or mutate the documents
	 * <p>
	 * With provided class reference for serialization/deserialization Rapid is able to automatically map document properties
	 * to object properties in Java class. By default the documents are serialized/deserialized by Gson, but you can provide own
	 * implementation of {@link RapidJsonConverter} via {@link Rapid#setJsonConverter(RapidJsonConverter)} method
	 * <p>
	 * If you don't want to convert data to a Java class, you can call {@link Rapid#collection(String)} and data
	 * will be provided as a {@link Map}<String, Object>
	 *
	 * @param collectionName collection name
	 * @param itemClass      class used for serializing/deserializing documents
	 * @return collection reference
	 */
	public <T> RapidCollectionReference<T> collection(String collectionName, Class<T> itemClass) {
		return mCollectionProvider.provideCollection(collectionName, itemClass);
	}


	/**
	 * Get collection reference {@link RapidCollectionReference} to create subscriptions and/or mutate the documents
	 * <p>
	 * Data will be provided as a {@link Map}<String, Object>
	 * <p>
	 * If you want Rapid to convert JSON automatically to Java objects, use {@link Rapid#collection(String, Class)}
	 *
	 * @param collectionName collection name
	 * @return collection reference
	 */
	public RapidCollectionReference<Map<String, Object>> collection(String collectionName) {
		return mCollectionProvider.provideCollection(collectionName);
	}


	public <T> RapidChannelReference<T> channel(String channelName, Class<T> messageClass) {
		return ((RapidChannelReference) mCollectionProvider.provideChannel(channelName, messageClass, false));
	}


	public <T> RapidChannelPrefixReference<T> channels(String channelNamePrefix, Class<T> messageClass) {
		return mCollectionProvider.provideChannel(channelNamePrefix, messageClass, true);
	}


	/**
	 * Authorize with an authorization token
	 * <p>
	 * Data from collection are readable/writable/editable based on rules included inside the token itself.
	 *
	 * @param authorizationToken Authorization token
	 * @return RapidFuture for handling result of the process
	 */
	public RapidFuture authorize(String authorizationToken) {

		return mRapidConnection.authorize(authorizationToken);
	}


	/**
	 * Cancels existing authorization
	 *
	 * @return RapidFuture for handling result of the process
	 */
	public RapidFuture deauthorize() {

		return mRapidConnection.deauthorize();
	}


	/**
	 * Get a difference between local device time and server time in milliseconds
	 * <p>
	 * When server time is 1.1.2017 7:18:19 AM and device time is 1.1.2017 7:18:20
	 * the offset is positive 1000
	 * <p>
	 * Offset's accuracy can be affected by network latency, so it is useful primarily for discovering large (> 1 second) discrepancies in clock time
	 *
	 * @param callback called with time offset in milliseconds
	 */
	public RapidFuture getServerTimeOffset(RapidCallback.TimeOffset callback) {
		return mRapidConnection.getSetverTimeOffset(callback);
	}


	/**
	 * Get JSON converter used for serialization and deserialization to/from Java objects.
	 * <p>
	 * By default, Gson is used for this task. Use {@link Rapid#setJsonConverter(RapidJsonConverter)} to provide custom converter
	 *
	 * @return RapidJsonConverter
	 */
	public RapidJsonConverter getJsonConverter() {
		return mJsonConverter.get();
	}


	/**
	 * Set JSON converter used for serialization and deserialization to/from Java objects.
	 * <p>
	 * By default, Gson is used for this task
	 *
	 * @param jsonConverter Custom JSON converter
	 */
	public void setJsonConverter(RapidJsonConverter jsonConverter) {
		mJsonConverter.set(jsonConverter);
	}


	/**
	 * Get Rapid.io API key this instance was initialized with
	 *
	 * @return Rapid.io API key
	 */
	public String getApiKey() {
		return mApiKey;
	}


	/**
	 * Method for setting connection state listener.
	 * <p>
	 * Possible states:
	 * <p>
	 * {@link ConnectionState#CONNECTED}
	 * <p>
	 * {@link ConnectionState#CONNECTING}
	 * <p>
	 * {@link ConnectionState#DISCONNECTED}
	 */
	public void addConnectionStateListener(RapidConnectionStateListener listener) {
		mRapidConnection.addConnectionStateListener(listener);
	}


	/**
	 * Method for removing connection state listener.
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
	 * Get current connection state
	 * <p>
	 * Possible states:
	 * <p>
	 * {@link ConnectionState#CONNECTED}
	 * <p>
	 * {@link ConnectionState#CONNECTING}
	 * <p>
	 * {@link ConnectionState#DISCONNECTED}
	 */
	public ConnectionState getConnectionState() {
		return mRapidConnection.getConnectionState();
	}


	/**
	 * Method for enabling/disabling subscription disk cache
	 *
	 * @param cachingEnabled
	 */
	public void setCachingEnabled(boolean cachingEnabled) {
		mCollectionProvider.getSubscriptionDiskCache().setEnabled(cachingEnabled);
	}


	/**
	 * Adjust subscription disk cache size
	 * <p>
	 * Default size is 50 MB
	 *
	 * @param cacheSizeInMb Cache size in MB
	 */
	public void setCacheSize(int cacheSizeInMb) {
		mCollectionProvider.getSubscriptionDiskCache().setMaxSize(cacheSizeInMb);
	}


	/**
	 * Set level of Logcat output
	 * <p>
	 * {@link LogLevel#LOG_LEVEL_NONE} - no logs at all
	 * <p>
	 * {@link LogLevel#LOG_LEVEL_ERRORS} - log only errors
	 * <p>
	 * {@link LogLevel#LOG_LEVEL_WARNINGS} - log errors and warnings
	 * <p>
	 * {@link LogLevel#LOG_LEVEL_INFO} - log errors, warnings and informative messages useful for debugging
	 * <p>
	 * {@link LogLevel#LOG_LEVEL_VERBOSE} - log everything
	 *
	 * @param level desired log level
	 */
	public void setLogLevel(int level) {
		sLogger.setLevel(level);
	}


	/**
	 * Get current authentication state
	 *
	 * @return true when authenticated
	 */
	public boolean isAuthenticated() {
		return mRapidConnection.isAuthenticated();
	}


	/**
	 * Set network connection timeout for subscriptions and mutations
	 * <p>
	 * By default, Rapid SDK will keep trying to perform database operations until they are successful
	 *
	 * @param connectionTimeoutInMs Connection timeout in milliseconds
	 */
	public void setConnectionTimeout(long connectionTimeoutInMs) {
		mRapidConnection.setConnectionTimeout(connectionTimeoutInMs);
	}
}

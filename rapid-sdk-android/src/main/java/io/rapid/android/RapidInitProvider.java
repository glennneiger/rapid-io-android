package io.rapid.android;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import io.rapid.CacheProvider;
import io.rapid.DiskCache;
import io.rapid.LoggerOutput;
import io.rapid.MemoryCache;
import io.rapid.Rapid;
import io.rapid.RapidExecutor;
import io.rapid.utility.Sha1Utility;


public class RapidInitProvider extends ContentProvider {
	@Override
	public boolean onCreate() {
		Context context = getContext();

		String TAG = "Rapid.io";
		Rapid.setLoggerOutput(new LoggerOutput() {
			@Override
			public void error(String message, Throwable throwable) {
				Log.e(TAG, message, throwable);
			}


			@Override
			public void info(String message, Throwable throwable) {
				Log.i(TAG, message, throwable);
			}


			@Override
			public void warning(String message, Throwable throwable) {
				Log.w(TAG, message, throwable);
			}
		});


		Handler handler = new Handler();
		RapidExecutor executor = new AndroidRapidExecutor(handler);
		Rapid.setExecutor(executor);

		Rapid.setCacheProvider(new CacheProvider() {
			@Override
			public DiskCache getNewDiskCache(String apiKey) {
				try {
					return new AndroidDiskCache(new File(context.getCacheDir() + "/rapid/" + Sha1Utility.sha1(apiKey)), Config.CACHE_DEFAULT_SIZE_MB);
				} catch(IOException e) {
					throw new IllegalStateException("BaseCollectionSubscription cache could not be initialized", e);
				} catch(NoSuchAlgorithmException e) {
					throw new IllegalStateException("BaseCollectionSubscription cache could not be initialized", e);
				}
			}


			@Override
			public <T> MemoryCache<T> getNewMemoryCache(int maxValue) {
				return new AndroidMemoryCache<>(maxValue);
			}
		});

		// try to auto-init from AndroidManifest metadata
		try {
			ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle metaData = app.metaData;
			if(metaData != null) {
				String apiKey = metaData.getString(Config.API_KEY_METADATA);
				if(apiKey != null) {
					Rapid.initialize(apiKey);
				}
			}

		} catch(PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}


	@Override
	public void attachInfo(Context context, ProviderInfo providerInfo) {
		if(providerInfo == null) {
			throw new NullPointerException("RapidInitProvider ProviderInfo cannot be null.");
		}
		// So if the authorities equal the library internal ones, the developer forgot to set his applicationId
		if((BuildConfig.APPLICATION_ID + ".rapidinitprovider").equals(providerInfo.authority)) {
			throw new IllegalStateException("Incorrect provider authority in manifest. Most likely due to a "
					+ "missing applicationId variable in application\'s build.gradle.");
		}
		super.attachInfo(context, providerInfo);
	}


	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
		return null;
	}


	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}


	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
		return null;
	}


	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
		return 0;
	}


	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
		return 0;
	}
}

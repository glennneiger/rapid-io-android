package io.rapid;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


class RapidInitProvider extends ContentProvider {
	@Override
	public boolean onCreate() {
		Context context = getContext();
		Rapid.injectContext(context);
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

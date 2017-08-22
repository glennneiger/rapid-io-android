package io.rapid.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;


// requires android.permission.ACCESS_NETWORK_STATE
public final class NetworkUtility {
	private NetworkUtility() {}


	public static boolean isOnline(@NonNull Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected());
	}
}
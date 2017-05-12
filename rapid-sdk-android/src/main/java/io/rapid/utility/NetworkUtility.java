package io.rapid.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


// requires android.permission.ACCESS_NETWORK_STATE
public final class NetworkUtility
{
	private NetworkUtility() {}


	public static boolean isOnline(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected());
	}
}
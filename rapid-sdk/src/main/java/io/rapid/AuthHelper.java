package io.rapid;

import android.os.Handler;

import static io.rapid.ConnectionState.DISCONNECTED;
import static io.rapid.RapidError.ErrorType.INVALID_AUTH_TOKEN;


class AuthHelper
{
	private final Handler mOriginalThreadHandler;
	private final RapidLogger mLogger;
	private AuthCallback mCallback;

	private String mAuthToken;
	private boolean mAuthenticated = false;
	private boolean mPendingAuth = false;
	private RapidFuture mAuthFuture;
	private boolean mPendingDeauth = false;


	interface AuthCallback
	{
		RapidFuture sendAuthMessage(String token);
		RapidFuture sendDeauthMessage();
	}


	AuthHelper(Handler originalThreadHandler, AuthCallback callback, RapidLogger logger)
	{
		mOriginalThreadHandler = originalThreadHandler;
		mCallback = callback;
		mLogger = logger;
	}


	boolean isAuthenticated()
	{
		return mAuthenticated;
	}


	RapidFuture deauthorize(ConnectionState connectionState)
	{
		mLogger.logI("Deauthorizing");
		RapidFuture deauthFuture;
		if(mAuthToken == null || !mAuthenticated || connectionState == DISCONNECTED)
		{
			mPendingDeauth = false;
			mAuthToken = null;
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.invokeSuccess();
			mLogger.logI("Deauthorization successful");
			return future;
		}
		else
		{
			mPendingDeauth = true;
			deauthFuture = mCallback.sendDeauthMessage();
		}
		return deauthFuture;
	}


	RapidFuture authorize(String token)
	{
		mLogger.logI("Authorizing with token '%s'", token);
		if(token == null)
		{
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			RapidError error = new RapidError(INVALID_AUTH_TOKEN);
			mLogger.logE(error);
			future.invokeError(error);
			return future;
		}
		else if(!token.equals(mAuthToken) || (!mAuthenticated && !mPendingAuth) )
		{
			mAuthToken = token;
			mPendingAuth = true;
			mAuthFuture = mCallback.sendAuthMessage(mAuthToken);
		}
		else if(mAuthenticated)
		{
			mLogger.logI("Already authorized with the same token");
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.invokeSuccess();
			return future;
		}
		return mAuthFuture;
	}


	String getAuthToken()
	{
		return mAuthToken;
	}


	boolean isAuthRequired()
	{
		return mAuthToken != null && !mPendingAuth && !mAuthenticated;
	}


	boolean isAuthPending()
	{
		return mPendingAuth && !mPendingDeauth;
	}


	boolean isDeauthPending()
	{
		return mPendingAuth;
	}


	void setAuthPending()
	{
		mPendingAuth = true;
	}


	void authSuccess()
	{
		mLogger.logI("Authorization successful");
		mAuthenticated = true;
		mPendingAuth = false;
	}


	void authError()
	{
		mPendingAuth = false;
	}


	void deauthSuccess()
	{
		mLogger.logI("Deauthorization successful");
		mAuthenticated = false;
		mPendingDeauth = false;
	}


	void deauthError()
	{
		mPendingDeauth = false;
	}


	void onClose()
	{
		mPendingAuth = false;
		mPendingDeauth = false;
		mAuthenticated = false;
	}
}

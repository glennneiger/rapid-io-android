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
		void sendAuthMessage();
		void sendDeauthMessage();
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
		RapidFuture deauthFuture = new RapidFuture(mOriginalThreadHandler);
		if(mAuthToken == null || !mAuthenticated || connectionState == DISCONNECTED)
		{
			mPendingDeauth = false;
			mAuthToken = null;
			deauthFuture.invokeSuccess();
			mLogger.logI("Deauthorization successful");
		}
		else
		{
			mPendingDeauth = true;
			mCallback.sendDeauthMessage();
		}
		return deauthFuture;
	}


	RapidFuture authorize(String token)
	{
		mAuthFuture = new RapidFuture(mOriginalThreadHandler);
		mLogger.logI("Authorizing with token '%s'", token);
		if(token == null)
		{
			RapidError error = new RapidError(INVALID_AUTH_TOKEN);
			mLogger.logE(error);
			mAuthFuture.invokeError(error);
		}
		else if(!token.equals(mAuthToken) || (!mAuthenticated && !mPendingAuth) )
		{
			mAuthToken = token;
			mPendingAuth = true;
			mCallback.sendAuthMessage();
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
		return mPendingDeauth;
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
		if(mAuthFuture != null) mAuthFuture.invokeSuccess();
		mAuthFuture = null;
	}


	void authError(RapidError err)
	{
		mPendingAuth = false;
		if(mAuthFuture != null) mAuthFuture.invokeError(err);
		mAuthFuture = null;
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
		mAuthenticated = false;
	}
}

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
	private boolean mPendingUnauth = false;


	interface AuthCallback
	{
		RapidFuture sendAuthMessage(String token);
		RapidFuture sendUnauthMessage();
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


	RapidFuture unauthorize(ConnectionState connectionState)
	{
		mLogger.logI("Unauthorizing");
		RapidFuture unauthFuture;
		if(mAuthToken == null || !mAuthenticated || connectionState == DISCONNECTED)
		{
			mPendingUnauth = false;
			mAuthToken = null;
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.invokeSuccess();
			mLogger.logI("Unauthorization successful");
			return future;
		}
		else
		{
			mPendingUnauth = true;
			unauthFuture = mCallback.sendUnauthMessage();
		}
		return unauthFuture;
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
		return mPendingAuth && !mPendingUnauth;
	}


	boolean isUnauthPending()
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


	void unauthSuccess()
	{
		mLogger.logI("Unauthorization successful");
		mAuthenticated = false;
		mPendingUnauth = false;
	}


	void unauthError()
	{
		mPendingUnauth = false;
	}


	void onClose()
	{
		mPendingAuth = false;
		mPendingUnauth = false;
		mAuthenticated = false;
	}
}

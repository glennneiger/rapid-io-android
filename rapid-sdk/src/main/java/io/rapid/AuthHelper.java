package io.rapid;

import android.os.Handler;

import static io.rapid.ConnectionState.DISCONNECTED;
import static io.rapid.RapidError.ErrorType.INVALID_AUTH_TOKEN;


/**
 * Created by Leos on 28.04.2017.
 */

class AuthHelper
{
	private final Handler mOriginalThreadHandler;
	private AuthCallback mCallback;

	private String mAuthToken;
	private boolean mAuthenticated = false;
	private boolean mPendingAuth = false;
	private RapidFuture mAuthFuture;
	private boolean mPendingUnauth = false;
	private RapidFuture mUnauthFuture;


	interface AuthCallback
	{
		RapidFuture sendAuthMessage(String token);
		RapidFuture sendUnauthMessage();
	}


	AuthHelper(Handler originalThreadHandler, AuthCallback callback)
	{
		mOriginalThreadHandler = originalThreadHandler;
		mCallback = callback;
	}


	boolean isAuthenticated()
	{
		return mAuthenticated;
	}


	RapidFuture unauthorize(ConnectionState connectionState)
	{
		if(mAuthToken == null || !mAuthenticated || connectionState == DISCONNECTED)
		{
			mPendingUnauth = false;
			mAuthToken = null;
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.invokeSuccess();
			return future;
		}
		else
		{
			mPendingUnauth = true;
			mUnauthFuture = mCallback.sendUnauthMessage();
		}
		return mUnauthFuture;
	}


	RapidFuture authorize(String token)
	{
		if(token == null)
		{
			RapidFuture future = new RapidFuture(mOriginalThreadHandler);
			future.invokeError(new RapidError(INVALID_AUTH_TOKEN));
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
		mAuthenticated = true;
		mPendingAuth = false;
	}


	void authError()
	{
		mPendingAuth = false;
	}


	void unauthSuccess()
	{
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

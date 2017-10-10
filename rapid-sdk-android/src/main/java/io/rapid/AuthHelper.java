package io.rapid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.rapid.executor.RapidExecutor;

import static io.rapid.ConnectionState.DISCONNECTED;
import static io.rapid.RapidError.ErrorType.INVALID_AUTH_TOKEN;


class AuthHelper {
	private RapidExecutor mOriginalThreadHandler;
	private RapidLogger mLogger;
	private AuthCallback mCallback;
	private AuthTokenCallback mTokenCallback;

	@Nullable private String mAuthToken;
	private boolean mAuthenticated = false;
	private boolean mPendingAuth = false;
	private List<RapidFuture> mAuthFutureList = new ArrayList<>();
	private boolean mPendingDeauth = false;
	private RapidFuture mDeauthFuture;


	interface AuthCallback {
		void sendAuthMessage();
		void sendDeauthMessage();
	}


	interface AuthTokenCallback {
		void onTokenChanged();
	}


	AuthHelper() {}


	void setupAuthHelper(RapidExecutor originalThreadHandler, AuthCallback callback, RapidLogger logger) {
		mOriginalThreadHandler = originalThreadHandler;
		mCallback = callback;
		mLogger = logger;
	}


	boolean isAuthenticated() {
		return mAuthenticated;
	}


	@NonNull
	RapidFuture deauthorize(ConnectionState connectionState) {
		mLogger.logI("Deauthorizing");
		RapidFuture deauthFuture = new RapidFuture(mOriginalThreadHandler);
		if(mAuthToken == null || !mAuthenticated || connectionState == DISCONNECTED) {
			mPendingDeauth = false;
			deauthFuture.invokeSuccess();
			mLogger.logI("Deauthorization successful");
		} else {
			mPendingDeauth = true;
			mCallback.sendDeauthMessage();
		}
		mAuthToken = null;
		tokenChanged();
		mDeauthFuture = deauthFuture;
		return deauthFuture;
	}


	@Nullable
	RapidFuture authorize(@Nullable String token) {
		RapidFuture authFuture = new RapidFuture(mOriginalThreadHandler);
		mLogger.logI("Authorizing with token '%s'", token);
		if(token == null) {
			RapidError error = new RapidError(INVALID_AUTH_TOKEN);
			mLogger.logE(error);
			authFuture.invokeError(error);
			return authFuture;
		} else if(!token.equals(mAuthToken) || (!mAuthenticated && !mPendingAuth)) {
			mAuthToken = token;
			tokenChanged();
			mPendingAuth = true;
			mCallback.sendAuthMessage();
		} else if(mAuthenticated) {
			mLogger.logI("Already authorized with the same token");
			authFuture.invokeSuccess();
			return authFuture;
		}
		mAuthFutureList.add(authFuture);
		return authFuture;
	}


	@Nullable
	String getAuthToken() {
		return mAuthToken;
	}


	boolean isAuthRequired() {
		return mAuthToken != null && !mPendingAuth && !mAuthenticated;
	}


	boolean isAuthPending() {
		return mPendingAuth && !mPendingDeauth;
	}


	boolean isDeauthPending() {
		return mPendingDeauth;
	}


	void setAuthPending() {
		mPendingAuth = true;
	}


	public void setTokenCallback(AuthTokenCallback tokenCallback)
	{
		mTokenCallback = tokenCallback;
	}


	void authSuccess() {
		mLogger.logI("Authorization successful");
		mAuthenticated = true;
		mPendingAuth = false;
		for(RapidFuture future : mAuthFutureList) {
			future.invokeSuccess();
		}
		mAuthFutureList.clear();
	}


	void authError(RapidError err) {
		mPendingAuth = false;
		for(RapidFuture future : mAuthFutureList) {
			future.invokeError(err);
		}
		mAuthFutureList.clear();
	}


	void deauthSuccess() {
		mLogger.logI("Deauthorization successful");
		mAuthenticated = false;
		mPendingDeauth = false;
		mDeauthFuture.invokeSuccess();
	}


	void deauthError() {
		mPendingDeauth = false;
		mDeauthFuture.invokeError(new RapidError(RapidError.ErrorType.DEAUTH_ERROR));
	}


	void onClose() {
		mAuthenticated = false;
	}


	private void tokenChanged()	{
		if(mTokenCallback != null) mTokenCallback.onTokenChanged();
	}
}

package io.rapid;


import android.os.Handler;


public class RapidActionFuture extends RapidFuture{
	private final String mActionId;
	private RapidConnection mRapidConnection;


	RapidActionFuture(Handler handler, String actionId, RapidConnection rapidConnection) {
		super(handler);
		mActionId = actionId;
		mRapidConnection = rapidConnection;
	}

	public RapidFuture cancel(){
		return mRapidConnection.cancelOnDisconnect(mActionId);
	}


}

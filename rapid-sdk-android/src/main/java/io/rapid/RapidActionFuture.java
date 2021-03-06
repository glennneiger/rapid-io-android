package io.rapid;


import io.rapid.executor.RapidExecutor;


public class RapidActionFuture extends RapidFuture {
	private final String mActionId;
	private RapidConnection mRapidConnection;


	RapidActionFuture(RapidExecutor handler, String actionId, RapidConnection rapidConnection) {
		super(handler);
		mActionId = actionId;
		mRapidConnection = rapidConnection;
	}


	public RapidFuture cancel() {
		return mRapidConnection.cancelOnDisconnect(mActionId);
	}


}

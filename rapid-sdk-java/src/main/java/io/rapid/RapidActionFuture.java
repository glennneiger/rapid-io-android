package io.rapid;


public class RapidActionFuture extends RapidFuture{
	private final String mActionId;
	private RapidConnection mRapidConnection;


	RapidActionFuture(RapidExecutor executor, String actionId, RapidConnection rapidConnection) {
		super(executor);
		mActionId = actionId;
		mRapidConnection = rapidConnection;
	}

	public RapidFuture cancel(){
		return mRapidConnection.cancelOnDisconnect(mActionId);
	}


}

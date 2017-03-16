package io.rapid;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

import io.rapid.utility.Logcat;


/**
 * Created by Leos on 15.03.2017.
 */

public class WebSocketConnection extends WebSocketClient
{
	public WebSocketConnection(URI serverURI)
	{
		super(serverURI);
	}


	public WebSocketConnection(URI serverUri, Draft draft)
	{
		super(serverUri, draft);
	}


	public WebSocketConnection(URI serverUri, Draft draft, Map<String, String> headers, int connectTimeout)
	{
		super(serverUri, draft, headers, connectTimeout);
	}


	@Override
	public void onOpen(ServerHandshake handshakeData)
	{
		Logcat.d(handshakeData.getHttpStatusMessage() + " " + handshakeData.getHttpStatus());
	}


	@Override
	public void onMessage(String message)
	{
		Logcat.d(message);
	}


	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		Logcat.d(code + " " + reason + " " + remote);
	}


	@Override
	public void onError(Exception ex)
	{
		Logcat.d("error");
	}
}

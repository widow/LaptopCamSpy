package com.idoweinstein.servicehandler;

import org.java_websocket.WebSocket;

public interface IServiceHandler {
	public abstract void handleService(WebSocket webSocket, String message);
}

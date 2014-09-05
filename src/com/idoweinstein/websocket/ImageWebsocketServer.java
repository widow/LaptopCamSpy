package com.idoweinstein.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.idoweinstein.servicehandler.GrabImageServiceHandler;
import com.idoweinstein.servicehandler.IServiceHandler;

public class ImageWebsocketServer extends WebSocketServer {
	
	public static final int WEBSOCKET_PORT = 50099;
	
	IServiceHandler serviceHandler;

	public ImageWebsocketServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}
	
	public ImageWebsocketServer( InetSocketAddress address ) {
		super(address);
		System.out.println("ImageWebsocketServer::c'tor()");
	}


	@Override
	public void onClose(WebSocket webSocket, int code, String additionalParams, boolean remote) {
		System.out.println("ImageWebsocketServer::onClose");
		System.out.println("WebSocket: "+webSocket.getRemoteSocketAddress().toString());
		System.out.println("Code: "+code);
		System.out.println("Additional parameters: "+additionalParams);
		System.out.println("Initiated by: "+(remote ? "Remote":"Server"));
		

	}

	@Override
	public void onError(WebSocket webSocket, Exception exception) {
		System.out.println("ImageWebsocketServer::onError");
		System.out.println("WebSocket: "+webSocket.getRemoteSocketAddress().toString());
		System.out.println("Exception: "+exception.toString());
	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		System.out.println("ImageWebsocketServer::onMessage");
		System.out.println("WebSocket: "+webSocket.getRemoteSocketAddress().toString());
		System.out.println("Message: "+message);
		switch(message){
			case "grab":
				serviceHandler.handleService(webSocket, message); //TODO: get the service handler from a map with service name - service handler mappings (possible to add multiple service handlers)
				break;
		}

	}

	@Override
	public void onOpen(WebSocket webSocket, ClientHandshake clientHandShake) {
		System.out.println("ImageWebsocketServer::onOpen");
		System.out.println("WebSocket: "+webSocket.getRemoteSocketAddress().toString());
		System.out.println("ClientHandshake: "+clientHandShake.getResourceDescriptor());
	}
	
	public void setHandler(IServiceHandler serviceHandler){
		this.serviceHandler = serviceHandler;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException  {
		WebSocketImpl.DEBUG = true;
		int port = ImageWebsocketServer.WEBSOCKET_PORT; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
			System.out.println("No socket provided, keeping default: "+ImageWebsocketServer.WEBSOCKET_PORT);
		}
		ImageWebsocketServer s = new ImageWebsocketServer( port );
		s.setHandler(new GrabImageServiceHandler());
		s.start();
		System.out.println( "ImageWebsocketServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
//			s.sendToAll( in );
			if( in.equals( "exit" ) ) {
				System.out.println("Stopping");
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				System.out.println("Restarting");
				s.stop();
				s.start();
				break;
			}
		}
	}

}

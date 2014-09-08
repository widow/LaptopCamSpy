package com.widow.laptopcamspy.websocket;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.bytedeco.javacv.CanvasFrame;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

public class MyWebSocketClient extends JFrame implements ActionListener {
	private static final long serialVersionUID = -6056260699202978657L;

	private final JTextField uriField;
	private final JButton connect;
	private final JButton close;
	private final JButton start;
	private final JButton stop;
	private final JTextArea ta;
	private final JTextField chatField;
	private WebSocketClient cc;
	//create canvas frame named 'Demo'
	final CanvasFrame canvas = new CanvasFrame("My Canvas");

	public MyWebSocketClient( String defaultlocation ) {
		super( "WebSocket Chat Client" );
		Container c = getContentPane();
		GridLayout layout = new GridLayout();
		layout.setColumns( 1 );
		layout.setRows( 7 );
		c.setLayout( layout );

		uriField = new JTextField();
		uriField.setText( defaultlocation );
		c.add( uriField );

		connect = new JButton( "Connect" );
		connect.addActionListener( this );
		c.add( connect );

		close = new JButton( "Close" );
		close.addActionListener( this );
		close.setEnabled( false );
		c.add( close );
		
		JScrollPane scroll = new JScrollPane();
		ta = new JTextArea();
		scroll.setViewportView( ta );
		c.add( scroll );

		chatField = new JTextField();
		chatField.setText( "" );
		chatField.addActionListener( this );
		c.add( chatField );
		
		start = new JButton( "Start" );
		start.addActionListener( this );
		start.setEnabled( true );
		c.add( start );

		stop = new JButton( "Stop" );
		stop.addActionListener( this );
		stop.setEnabled( true );
		c.add( stop );

		java.awt.Dimension d = new java.awt.Dimension( 300, 400 );
		setPreferredSize( d );
		setSize( d );

		addWindowListener( new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing( WindowEvent e ) {
				if( cc != null ) {
					cc.close();
				}
				dispose();
			}
		} );

		setLocationRelativeTo( null );
		setVisible( true );
	}

	public void actionPerformed( ActionEvent e ) {
		if(e.getSource() == stop){
			cc.send( "stop" );
			chatField.setText( "stop" );
		}else
		if(e.getSource() == start){
			cc.send( "start" );
			chatField.setText( "start" );
		}else
		if( e.getSource() == chatField ) {
			if( cc != null ) {
				cc.send( chatField.getText() );
				chatField.setText( "" );
				chatField.requestFocus();
			}

		} else if( e.getSource() == connect ) {
			try {
				// cc = new ChatClient(new URI(uriField.getText()), area, ( Draft ) draft.getSelectedItem() );
				cc = new WebSocketClient( new URI( uriField.getText() ), new Draft_17() ) {

					@Override
					public void onMessage( String message ) {
						ta.append( "got: " + message + "\n" );
						ta.setCaretPosition( ta.getDocument().getLength() );
					}

					@Override
					public void onOpen( ServerHandshake handshake ) {
						ta.append( "You are connected to ChatServer: " + getURI() + "\n" );
						ta.setCaretPosition( ta.getDocument().getLength() );
					}

					@Override
					public void onClose( int code, String reason, boolean remote ) {
						ta.append( "You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n" );
						ta.setCaretPosition( ta.getDocument().getLength() );
						connect.setEnabled( true );
						uriField.setEditable( true );
						close.setEnabled( false );
					}

					@Override
					public void onError( Exception ex ) {
						ta.append( "Exception occured ...\n" + ex + "\n" );
						ta.setCaretPosition( ta.getDocument().getLength() );
						ex.printStackTrace();
						connect.setEnabled( true );
						uriField.setEditable( true );
						close.setEnabled( false );
					}
					
					@Override
					public void onMessage( ByteBuffer bytes ) {
//						System.out.println("got "+ bytes.capacity()+ "bytes");
						ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.array());
					
						BufferedImage image = null;
						try {
							image = ImageIO.read(inputStream);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						//set canvas title
						canvas.setTitle(bytes.capacity()+ "bytes");
						
						//Show image in canvas frame
						canvas.showImage(image);
					}
				};

				close.setEnabled( true );
				connect.setEnabled( false );
				uriField.setEditable( false );
				cc.connect();
			} catch ( URISyntaxException ex ) {
				ta.append( uriField.getText() + " is not a valid WebSocket URI\n" );
			}
		} else if( e.getSource() == close ) {
			cc.close();
		}
	}
	
	public static void main( String[] args ) {
		WebSocketImpl.DEBUG = false;
		String location;
		if( args.length != 0 ) {
			location = args[ 0 ];
			System.out.println( "Default server url specified: \'" + location + "\'" );
		} else {
			location = "ws://localhost:"+ImageWebsocketServer.WEBSOCKET_PORT;
			System.out.println( "Default server url not specified: defaulting to \'" + location + "\'" );
		}
		new MyWebSocketClient( location );
	}

}

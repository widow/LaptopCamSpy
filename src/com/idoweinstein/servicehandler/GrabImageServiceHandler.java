package com.idoweinstein.servicehandler;

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

	@Override
	public void handleService(WebSocket webSocket, String message) {
		
		//laptop camera image
		FrameGrabber grabber = new VideoInputFrameGrabber(0);
		
		try {
			grabber.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Load image img1 as IplImage
		IplImage image = null;
		try {
			image = grabber.grab();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			grabber.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//convert image to base64 string and send
		BufferedImage buffImage = image.getBufferedImage();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(buffImage, "jpg", outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteBuffer byteBuff = ByteBuffer.wrap(outputStream.toByteArray());
		System.out.println("sent "+ byteBuff.capacity()+ "bytes");
		webSocket.send(byteBuff);
		
	}
}

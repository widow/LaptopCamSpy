package com.widow.laptopcamspy.servicehandler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.java_websocket.WebSocket;

import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;


public class GrabImageServiceHandler implements IServiceHandler {


	public static final String XML_FILE = "resources/haarcascade_frontalface_default.xml";
	Thread t;
	FrameGrabber grabber = new VideoInputFrameGrabber(0);
	final CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(XML_FILE));

	@Override
	public void handleService(final WebSocket webSocket, String message) {

		switch (message){
		case "start":;
		System.out.println("Starting...");
		t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					IplImage image = null;
					grabber.start();

					while(t.isInterrupted() == false){

						image = grabber.grab();

						CvMemStorage storage = CvMemStorage.create();
						CvSeq sign = cvHaarDetectObjects(
								image,
								cascade,
								storage,
								1.5,
								3,
								CV_HAAR_MAGIC_VAL);

						cvClearMemStorage(storage);

						int total_Faces = sign.total();	
						System.out.println("Found total of: "+total_Faces + " faces");
						for(int i = 0; i < total_Faces; i++){
							CvRect r = new CvRect(cvGetSeqElem(sign, i));
							cvRectangle (
									image,
									cvPoint(r.x(), r.y()),
									cvPoint(r.width() + r.x(), r.height() + r.y()),
									CvScalar.RED,
									2,
									CV_AA,
									0);

						}

						//convert image to base64 string and send
						BufferedImage buffImage = image.getBufferedImage();

						ImageIO.write(buffImage, "jpg", outputStream);

						ByteBuffer byteBuff = ByteBuffer.wrap(outputStream.toByteArray());
						outputStream.reset();
						//System.out.println("sent "+ byteBuff.capacity()+ "bytes");
						webSocket.send(byteBuff);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						grabber.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
		t.start();
		break;
		case "stop":
			System.out.println("Stopping");
			t.interrupt();
			break;
		}
	}
}



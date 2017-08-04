
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;
public class test_opencv {
	private CascadeClassifier eyeDetector = new CascadeClassifier(FaceDetector.class.getResource("haarcascade_eye_tree_eyeglasses.xml").getPath());
	private MatOfRect detectedEyes = new MatOfRect();
	
	public static void main(String[] args) 
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
		test_opencv detector = new test_opencv();
        detector.detect();
	}
	private void detect() {
        Mat mat = new Mat();
        VideoCapture camera = new VideoCapture(0);
        final Size frameSize = new Size(camera.get(Highgui.CV_CAP_PROP_FRAME_WIDTH), camera.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel panel = new JLabel();
        frame.setContentPane(panel);
        frame.setSize((int) frameSize.width, (int) frameSize.height);
        frame.setVisible(true);
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(mat, camera));
        while (camera.isOpened() && camera.read(mat)) {
            
            eyeDetector.detectMultiScale(mat, detectedEyes);
            
            for (Rect eye : detectedEyes.toArray()) {
                if(eye.height == 0 && eye.width == 0){
                    continue;
                }
                Core.rectangle(mat, new Point(eye.x, eye.y), new Point(eye.x + eye.width, eye.y + eye.height),
                        new Scalar(255, 12, 0), 3);
            }
            ImageIcon image = new ImageIcon(mat2BufferedImage(mat));
            panel.setIcon(image);
            panel.repaint();
        }
    }
    private BufferedImage mat2BufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return img;
    }
    private class ShutdownThread extends Thread {
        private Mat mat;
        private VideoCapture videoCapture;
        public ShutdownThread(Mat mat, VideoCapture videoCapture) {
            this.mat = mat;
            this.videoCapture = videoCapture;
        }
        @Override
        public void run() {
            super.run();
            mat.release();
            videoCapture.release();
        }
    }
}
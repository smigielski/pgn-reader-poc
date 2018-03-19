import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by marek on 28/05/16.
 */
public class ArUcoTest {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void toBufferedImage(Mat m, String fileName) throws IOException {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        File f = new File(fileName);
        ImageIO.write(image, "PNG", f);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat: " + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());


        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
//        Dictionary dictionary = Dictionary.create(250,6);
        System.out.println("Marker size:" + dictionary.get_markerSize());
        System.out.println("Coorection bits:" + dictionary.get_maxCorrectionBits());
        System.out.println("OpenCV Mat data:\n" + dictionary.get_bytesList().dump());

for (int i = 0; i<50; i++) {
    Mat img = new Mat();
    dictionary.drawMarker(i, 6, img, 1);
//        Aruco.drawMarker(dictionary, 22, 200, img);

    toBufferedImage(img, "markers/markers4X4_50_"+i+".png");
}
    }
//mogrify -fuzz 0% -fill 'rgb(57,57,57)' -opaque black *png
}
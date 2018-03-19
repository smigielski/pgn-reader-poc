import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by marek on 30/05/16.
 */
public class DetectCorners {
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

    public static final void main(String[] args) throws IOException {
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
        Mat image = Imgcodecs.imread(DetectCorners.class.getClass().getResource("/IMG_2012.png").getPath());
        ArrayList<Mat> markerCorners = new ArrayList<Mat>();
        Mat markerIds = new Mat();
        Aruco.detectMarkers(image,dictionary,markerCorners,markerIds);

        System.out.println("OpenCV Mat data:\n" + markerIds.dump());
        System.out.println("OpenCV Mat data:\n" + markerCorners.get(0).cols()+", "+ markerCorners.get(0).rows()+ ", "+ markerCorners.get(0).type() );


        Mat img = new Mat( 4, 1, 13 );
        int row = 0, col = 0;
        img.put(row ,col, 0, 0, 100, 0, 100,100,0,100);

        Mat transformation = Imgproc.getPerspectiveTransform( markerCorners.get(0),img );

        Mat bigImage = new Mat(100,100, image.type());

        Imgproc.warpPerspective(image, bigImage, transformation, bigImage.size());

        toBufferedImage(bigImage,"test.png");
    }
}

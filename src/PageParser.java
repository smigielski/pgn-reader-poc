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
public class PageParser {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static int scale = 4;

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

//        Dictionary dictionary = Dictionary.create(250,6);
        Mat image = Imgcodecs.imread(DetectCorners.class.getClass().getResource("/IMG_2022.JPG").getPath());
//        Mat gray = new Mat();
//        image.convertTo(gray, CvType.CV_8UC1);
//        Imgproc.cvtColor(image, gray, 6);


//        Imgproc.adaptiveThreshold(gray, gray, 240, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);
//        Core.bitwise_not( gray, gray );
//        Core.multiply(gray,new Scalar(2),gray);
//        Core.add(gray,new Scalar(100),gray);
//        System.out.println("threshold");
//        toBufferedImage(gray,"invert5.png");
//        System.out.println("saved");

        Mat bigImage = transform(image);

        toBufferedImage(bigImage, "test.png");

    }

    public static Mat transform(Mat image) {
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_50);

        ArrayList<Mat> markerCorners = new ArrayList<Mat>();
        Mat markerIds = new Mat();
        Aruco.detectMarkers(image,dictionary,markerCorners,markerIds);


        Mat picture = new Mat(4, 1, 13);
        int row = 0, col = 0;
        picture.put(row, col, markerCorners.get(match(markerIds,2)).get(0,0)[0], markerCorners.get(match(markerIds,2)).get(0,0)[1],
                markerCorners.get(match(markerIds,3)).get(0,1)[0], markerCorners.get(match(markerIds,3)).get(0,1)[1],
                markerCorners.get(match(markerIds,0)).get(0,2)[0], markerCorners.get(match(markerIds,0)).get(0,2)[1],
                markerCorners.get(match(markerIds,1)).get(0,3)[0], markerCorners.get(match(markerIds,1)).get(0,3)[1]);

        Mat img = new Mat(4, 1, 13);
        img.put(row, col, 21*scale, 21*scale, 509*scale, 21*scale, 509*scale, 722*scale, 21*scale, 722*scale);

        Mat transformation = Imgproc.getPerspectiveTransform(picture, img);

        Mat bigImage = new Mat( 745*scale,525*scale, image.type());

        Imgproc.warpPerspective(image, bigImage, transformation, bigImage.size());
        return bigImage;
    }

    private static int match(Mat markerIds, int match) {
        for (int i =0;i<markerIds.rows();i++){
            if (markerIds.get(i,0)[0]==match){
                return i;
            }
        }
        return -1;
    }



}

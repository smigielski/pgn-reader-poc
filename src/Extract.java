import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marek on 02/06/16.
 */
public class Extract {
    public static final double SIZE = 20;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    private static Rect text1 = new Rect(204, 758, 352, 1706);
    private static int columns = 4;
    private static int rows = 20;



    public static void main(String[] args) throws IOException {

        Mat image = Imgcodecs.imread(DetectCorners.class.getClass().getResource("/test.png").getPath());
        Imgproc.cvtColor(image, image, 6);



        List<Mat> moves = moves(image, text1, columns, rows);
        for (int i = 0; i < columns * rows; i++) {
            toBufferedImage(moves.get(i), "reader/" + String.format("%02d", (i / columns + 1)) + "-" + (i % columns) + ".png");
        }

    }

    public static List<Mat> moves(Mat image, Rect white, int columns, int rows) {
        List<Mat> result = new ArrayList<>();
        int width = white.width / columns;
        int height = white.height / rows;


        Imgproc.blur(image, image, new Size(8, 8));

        for (int j = 0; j < rows; j++)
            for (int i = 0; i < columns; i++) {

                Mat letter = new Mat(image, new Rect(white.x + i * width, white.y + j * height, width, height));
                Mat edges = new Mat();
//            Imgproc.Canny(letter,edges,100,200);
//            letter.convertTo(letter, CvType.CV_8UC1);
                Imgproc.adaptiveThreshold(letter, edges, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);

                cleanVerticalLines(edges);


                clean(edges);


                edges = scale(edges);
                result.add(edges);

            }

        return result;
    }

    public static Mat scale(Mat edges) {
        Mat threshold_output = new Mat();
        /// Detect edges using Threshold
        Imgproc.threshold(edges, threshold_output, 100, 255, Imgproc.THRESH_BINARY);
        /// Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold_output, contours, hierarchy, 3,
                2, new Point(0, 0));

/// Approximate contours to polygons + get bounding rects and circles
//            vector<vector<Point> > contours_poly( contours.size() );
//            vector<Rect> boundRect( contours.size() );
//            vector<Point2f>center( contours.size() );

        List<Point> all = new ArrayList<>();

        for (int k = 0; k < contours.size(); k++) {
            all.addAll(contours.get(k).toList());

//                Imgproc.approxPolyDP(  contours.get(k), contours_poly[i], 3, true );

        }

        MatOfPoint allPoints = new MatOfPoint();
        allPoints.fromList(all);

        Rect rect = Imgproc.boundingRect(allPoints);

        if (rect.width > 0 && rect.height > 0) {

            int longer;
            if (rect.width > rect.height) {
                longer = rect.width;

            } else {
                longer = rect.height;

            }

            double scale = SIZE / (double) longer;

            Scalar color = new Scalar(255, 0, 0);
            Mat resized = new Mat();
            Imgproc.resize(edges, resized, new Size(Math.ceil(edges.width() * scale), Math.ceil(edges.height() * scale)));
            double x1 = (rect.tl().x - (longer - rect.width) / 2) * scale;
            if (x1 + longer * scale > resized.width()) {
                x1 = resized.width() - longer * scale;
            }
            if (x1 < 0) {
                x1 = 0;
            }
            double y1 = (rect.tl().y - (longer - rect.height)) * scale;
            if (y1 + longer * scale > resized.height()) {
                y1 = resized.height() - longer * scale;
            }
            if (y1 < 0) {
                y1 = 0;
            }

            Rect crop = new Rect(new Point(x1, y1), new Size(longer * scale, longer * scale));

//            Imgproc.rectangle(edges, rect.tl(), rect.br(), color, 2, 8, 0);
//            System.out.println(rect);
//            System.out.println(resized.size());
//            System.out.println(crop);
            return new Mat(resized, crop);
        } else {
            Imgproc.resize(edges, edges, new Size(SIZE, SIZE));
            return edges;
        }
    }

    private static void cleanVerticalLines(Mat edges) {
        Mat lines = new Mat();
        Mat cdst = new Mat(edges.size(), edges.type(), new Scalar(255));
        // detect lines
        Imgproc.HoughLines(edges, lines, 1.0, Math.PI / 180, 60);

        // draw lines
        for (int w = 0; w < lines.rows(); w++) {
            double rho = lines.get(w, 0)[0];
            double theta = lines.get(w, 0)[1];
            if ((theta > Math.PI / 180 * 170 || theta < Math.PI / 180 * 10) ||
                    theta > Math.PI / 180 * 80 && theta < Math.PI / 180 * 100) {

                Point pt1 = new Point();
                Point pt2 = new Point();
                double a = Math.cos(theta), b = Math.sin(theta);
                double x0 = a * rho, y0 = b * rho;

                //check rectangle
                double border = 0.2;

//                    \rho = x \cos \theta + y \sin \theta

                double p1 = edges.width() * border * a + edges.height() * border * b - rho;
                double p2 = edges.width() * border * a + edges.height() * (1 - border) * b - rho;
                double p3 = edges.width() * (1 - border) * a + edges.height() * border * b - rho;
                double p4 = edges.width() * (1 - border) * a + edges.height() * (1 - border) * b - rho;

                if ((p1 < 0 && p2 < 0 && p3 < 0 && p4 < 0) || (p1 > 0 && p2 > 0 && p3 > 0 && p4 > 0)) {

                    pt1.x = (x0 + 1000 * (-b));
                    pt1.y = (y0 + 1000 * (a));
                    pt2.x = (x0 - 1000 * (-b));
                    pt2.y = (y0 - 1000 * (a));

                    Imgproc.line(edges, pt1, pt2, new Scalar(0, 0, 0), 3);

                }
            }
        }
    }

    private static void clean(Mat edges) {
        //        int verticalsize = vertical.rows() / 1;
        // Create structure element for extracting vertical lines through morphology operations
        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        // Apply morphology operations
        Imgproc.erode(edges, edges, verticalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(edges, edges, verticalStructure, new Point(-1, -1), 1);
    }


//    private static Mat crop(Mat img, Rect rect) {
////        System.out.println(rect);
//        return new Mat(img, rect);
//    }

    public static void toBufferedImage(Mat m, String fileName) {
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
        try {
            ImageIO.write(image, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

//
//
//    Mat horizontal = edges.clone();
//    Mat vertical = edges.clone();
//
//    // Specify size on horizontal axis
//    int horizontalsize = horizontal.cols() / 1;
//    // Create structure element for extracting horizontal lines through morphology operations
//    Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
//// Apply morphology operations
//Imgproc.erode(horizontal, horizontal, horizontalStructure, new Point(-1, -1),1);
//        Imgproc.dilate(horizontal, horizontal, horizontalStructure, new Point(-1, -1),1);
//
//        // Specify size on vertical axis
//        int verticalsize = vertical.rows() / 1;
//        // Create structure element for extracting vertical lines through morphology operations
//        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 1,verticalsize));
//        // Apply morphology operations
//        Imgproc.erode(vertical, vertical, verticalStructure, new Point(-1, -1),1);
//        Imgproc.dilate(vertical, vertical, verticalStructure, new Point(-1, -1),1);
//
//        Core.bitwise_not(vertical, vertical);
//
//        vertical.copyTo(edges);
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.opencv.ml.StatModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by marek on 31/05/16.
 */
public class HandWritingRecognition {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static int k = 90;

    private final KNearest knn = KNearest.create();
    private final String index;

    public HandWritingRecognition(String dictionary, String index, int count){
        this(dictionary,index,count,index);
    }

    public HandWritingRecognition(String dictionary, String index, int count,String subset){
        this.index=index;
        Mat img = Imgcodecs.imread(DetectCorners.class.getClass().getResource(dictionary).getPath());
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);


        Mat samples = new Mat(0,0,CvType.CV_32F);
        Mat responses = new Mat();

        // for each img in the train set :
        for (int i =0; i<index.length(); i++) {
            for (int j = 0; j < count; j++) {
                if (subset.indexOf(index.charAt(i))>=0) {
//                System.out.println("i:"+ i + "j:"+ j);
                    Mat digit = crop(img, new Rect(j * 100, i * 100, 100, 100));
                    Imgproc.cvtColor(digit, digit, 6);
                    digit = Extract.scale(digit);
                    Mat floatImage = new Mat();

//                    Extract.toBufferedImage(digit, "writer/" + i + j + ".png");

                    digit.convertTo(floatImage, CvType.CV_32F);             // to float

//                System.out.println("OpenCV Mat cols:" + floatImage.cols()+ " rows:" + floatImage.rows() + " type: " + floatImage.type());

                    samples.push_back(floatImage.reshape(1, 1)); // add 1 row (flattened image)

                    Mat train = new Mat(1, 1, CvType.CV_32F);
                    train.setTo(new Scalar(i));
                    responses.push_back(train);
                }
            }
        }

        System.out.println("OpenCV Mat cols:" + samples.cols()+ " rows:" + samples.rows() + " type: " + samples.type());
        System.out.println("OpenCV Mat cols:" + responses.cols()+ " rows:" + responses.rows() + " type: " + responses.type());
//        knn.setDefaultK(33);

//        System.out.println(responses.dump());

        knn.train(samples, Ml.ROW_SAMPLE,responses);
    }

    public String detect(Mat... array){

        Mat real = new Mat(0,0,CvType.CV_32F);
        Mat responses = new Mat();

        for (Mat img : array) {
//                System.out.println("i:"+ i + "j:"+ j);
            Mat digit = new Mat();
               Imgproc.resize(img, digit, new Size(Extract.SIZE,Extract.SIZE));

//                Mat digit = crop(img, new Rect(j*20, i*20, 20, 20));
                Mat floatImage = new Mat();
                digit.convertTo(floatImage, CvType.CV_32F);             // to float
//            System.out.println("OpenCV Mat cols:" + floatImage.cols()+ " rows:" + floatImage.rows() + " type: " + floatImage.type());
                real.push_back(floatImage.reshape(1, 1)); // add 1 row (flattened image)
//                 Mat response = new Mat(1,1,CvType.CV_32F);
//                responses.push_back(response);
        }

//        System.out.println("OpenCV Mat cols:" + real.cols()+ " rows:" + real.rows() + " type: " + real.type());
//        System.out.println("OpenCV Mat cols:" + responses.cols()+ " rows:" + responses.rows() + " type: " + responses.type());
//
        knn.findNearest(real, 5, responses);
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i<responses.rows();i++){
            sb.append(index.charAt(Double.valueOf(responses.get(i,0)[0]).intValue()));
        }
        return sb.toString();
    };

    public static final void main(String[] args) throws IOException {

        HandWritingRecognition handWritingRecognition = new HandWritingRecognition("/result.png", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",55,"0123456789");

        Mat img = Imgcodecs.imread(DetectCorners.class.getClass().getResource("/digits.png").getPath());
//        knn.train(train,train_labels)
//        ret,result,neighbours,dist = knn.find_nearest(test,k=5)

        Mat real = new Mat(0,0,CvType.CV_32F);

                int success = 0;
        int count = 0;

        for (int i =0; i<50; i++) {
            List<Mat> mats = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
//                System.out.println("i:"+ i + "j:"+ j);
                Mat digit = crop(img, new Rect(j*20, i*20, 20, 20));
                Imgproc.cvtColor(digit, digit, 6);
                digit = Extract.scale(digit);
                mats.add(digit);


//                responses.push_back(train);
            }
            String detected = handWritingRecognition.detect(mats.toArray(new Mat[0]));
//            System.out.println(detected);
            for (int k = 0;k<detected.length();k++) {
                count++;
                if (String.valueOf(detected.charAt(k)).equals(String.valueOf(i / 5))) {
                    success++;
                } else {
                    System.out.println(String.valueOf(detected.charAt(k)) + ":" + i / 5);
                }
            }

        }





//        System.out.println(responses.dump());


//
//
        System.out.println("success:"+success/(double)count*100.0);
//
//        # Now we check the accuracy of classification
//        # For that, compare the result with test_labels and check which are wrong
//                matches = result==test_labels
//        correct = np.count_nonzero(matches)
//        accuracy = correct*100.0/result.size
    }

    private static Mat crop(Mat img, Rect rect) {
//        System.out.println(rect);
        return new Mat(img, rect);
    }


}

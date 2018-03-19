import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marek on 02/06/16.
 */
public class ReadPgn {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static String[] dictionaries = {"KHWSG","abcdefgh","12345678","x"};
    private static HandWritingRecognition[] recognitions = new HandWritingRecognition[dictionaries.length];

    private static Rect[] white = {new Rect(204, 758, 352, 1706)};
    private static Rect[] black = {new Rect(678, 764, 342, 1698)};

    private static String[] whiteScoreMoves = {"e4","f4","Gc4","Kf1","Gxb5","Sf3","d3","Sh4","Sf5","g4","Wg3",
            "h4","h5","Hf3","Gxf4","Sc3","Sd5","Gd6","e5","Kc2"};

    private static String[] blackScoreMoves = {"e5","exf4","Hh4","b5","Sf6","Hh6","Sh5","Hg5","c6","Sh6","cxb5",
    "Hg6","Hg5","Sg8","Hf6","Gc5","Hxb2","Gxg1","Hxa1","Sa6"};


    private static int columns = 4;
    private static int rows = 20;

    public static void main(String[] args) {
        String fileName = "/IMG_2022.JPG";
        Mat baseImage = Imgcodecs.imread(DetectCorners.class.getClass().getResource(fileName).getPath());
        Mat transformedImage = PageParser.transform((Mat) baseImage);
        Imgproc.cvtColor(transformedImage, transformedImage, 6);
        List<Mat> whiteMoves = new ArrayList<>();
        whiteMoves.addAll(Extract.moves(transformedImage,white[0],columns,rows));
        List<Mat> blackMoves = new ArrayList<>();
        blackMoves.addAll(Extract.moves(transformedImage,black[0],columns,rows));

        for (int i =0;i<dictionaries.length;i++){
            recognitions[i] = new HandWritingRecognition("/result.png",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",55,dictionaries[i]);
        }
        int count = 0;
        int success = 0;
        for (int i=0;i<whiteScoreMoves.length;i++){
            String whiteMove = whiteScoreMoves[i];
            String blackMove = blackScoreMoves[i];

            StringBuilder sb = new StringBuilder();
            sb.append((i+1)+". ");
            for (int j =0;j<whiteMove.length();j++){
                count++;
                String detected = recognitions[contains(whiteMove.charAt(j))].detect(whiteMoves.get(i*4+j));
                sb.append(detected);
                if (detected.charAt(0)== whiteMove.charAt(j)){
                    success++;
                }
            }
            sb.append(" ("+whiteMove+")\t\t");


            for (int j =0;j<blackMove.length();j++){
                count++;
                String detected = recognitions[contains(blackMove.charAt(j))].detect(blackMoves.get(i*4+j));
                sb.append(detected);
                if (detected.charAt(0)== blackMove.charAt(j)){
                    success++;
                }
            }
            sb.append(" ("+blackMove+")");

            System.out.println(sb.toString());

        }

        System.out.println("success:"+success/(double)count*100.0);

    }

    private static int contains (char ch){
        for (int i =0;i<dictionaries.length;i++){
            if (dictionaries[i].indexOf(ch)>=0){
                return i;
            }
        }
        return -1;
    }
}

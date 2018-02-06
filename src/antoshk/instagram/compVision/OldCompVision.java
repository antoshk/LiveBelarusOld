/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//Класс содержит методы компьютерного зрения для обработки изображений инстаграма (использует порт сишной opencv на java от каких-то сторонних чуваков)

package antoshk.instagram.compVision;

import antoshk.live_belarus.Utils;
import java.net.*;
import java.awt.image.*;

import java.io.File;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 *
 * @author User
 */
public class OldCompVision {

    static private BufferedImage currentImg;
    static private BufferedImage changedImg;
    static private File haarFile;
    static public final String haarFrontalFaceDef = "http://antoshk.com/livebelarus/haar/haarcascade_frontalface_alt.xml";

    static public void init() {

    }

    static public BufferedImage getSourceImage() {
        return currentImg;
    }

    static public BufferedImage getChangedImage() {
        return changedImg;
    }

    static public boolean checkImage(String imgURL) {
        BufferedImage image;
        int totalCount;
        image = ImageLdrWrtr.loadImage(imgURL);
        if (hasCorrectSize(image)) {
            totalCount = findLines(image);
            //test(image);
            //totalCount = 0;
            if (totalCount > 0) {
                Utils.print("Найдено линий:" + totalCount);
            } else {
                return true;
            }

            /*totalCount = findElements(image,haarFrontalFaceDef);
            if (totalCount > 0){
                Utils.print("Найдено элементов:"+totalCount);
            } else {
                return true;
            }*/
        } else {
            Utils.print("Неправильный размер изображения:" + image.getHeight() + " x " + image.getWidth());
            currentImg = null;
            changedImg = null;
        }
        return false;
    }

    static IplImage toIplImage(BufferedImage bufImage) {
        OpenCVFrameConverter.ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
        IplImage iplImage = iplConverter.convert(java2dConverter.convert(bufImage));
        return iplImage;
    }

    static BufferedImage iplImageToBufImage(IplImage image) {
        OpenCVFrameConverter.ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
        BufferedImage bufImage = java2dConverter.convert(iplConverter.convert(image));
        return bufImage;
    }

    /*public static BufferedImage loadImage(String imgURL) {
        URL url;
        BufferedImage bufImage;
        try {
            url = new URL(imgURL);
            bufImage = ImageIO.read(url);

        } catch (Exception e) {
            Utils.print("Что-то не то с URL картинки");
            Utils.print(e.getMessage());
            System.exit(1);
            return null;
        }
        return bufImage;
    }*/

    static boolean isSuitable(String imgURL) {
        BufferedImage image = ImageLdrWrtr.loadImage(imgURL);
        return hasCorrectSize(image);
    }

    static boolean hasCorrectSize(BufferedImage img) {
        return ((img.getHeight() > 630) || (img.getWidth() > 630));
    }

    public static int findElements(BufferedImage bufImage, String haarURLStr) {
        IplImage image, grayImage;
        //BufferedImage bufImage = null;
        URL haarURL;
        //File file = null;

        try {
            if (haarFile == null) {

                haarURL = new URL(haarURLStr);
                //haarURL = new URL("http://antoshk.com/livebelarus/haar/HS.xml");
                haarFile = Loader.extractResource(haarURL, null, "classifier", ".xml");
                haarFile.deleteOnExit();
            } else {
                //Utils.print(haarFile.toURI().toString());     
                //if (haarFile.toURI().toString().equals(haarURLStr)){
                //  Utils.print("Файл уже загружен"); 
                //}    

            }
        } catch (Exception e) {
            Utils.print("Что-то не то с URL каскада");
            Utils.print(e.getMessage());
            System.exit(1);

        }

        String classifierName;
        classifierName = haarFile.getAbsolutePath();
        //classifierName = "C:\\haarcascade_frontalface_default.xml";

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);

        image = toIplImage(bufImage);
        grayImage = IplImage.create(bufImage.getWidth(), bufImage.getHeight(), IPL_DEPTH_8U, 1);
        cvCvtColor(image, grayImage, CV_BGR2GRAY);

        CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
        if (classifier.isNull()) {
            Utils.printWarn("Error loading classifier file \"" + classifierName + "\".");
            System.exit(1);
        }

        CvMemStorage storage = CvMemStorage.create();
        CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage,
                1.1, 5, 3);
        int total = faces.total();

        for (int i = 0; i < total; i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            cvRectangle(image, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.RED, 1, CV_AA, 0);
        }

        changedImg = iplImageToBufImage(image.clone());
        return total;
    }

    static int altFindElements(BufferedImage bufImage, String haarURLStr) {
        int total;
        Mat frame;
        IplImage image;
        image = toIplImage(bufImage);
        frame = cvarrToMat(image);
        RectVector faces = new RectVector();

        URL haarURL;
        try {
            if (haarFile == null) {
                haarURL = new URL(haarURLStr);
                haarFile = Loader.extractResource(haarURL, null, "classifier", ".xml");
                haarFile.deleteOnExit();
            }
        } catch (Exception e) {
            Utils.print("Что-то не то с URL каскада");
            Utils.print(e.getMessage());
            System.exit(1);

        }

        String classifierName;
        classifierName = haarFile.getAbsolutePath();

        CascadeClassifier faceDetector = new CascadeClassifier(classifierName);

        faceDetector.detectMultiScale(frame, faces);

        total = (int) faces.capacity();
        for (long i = 0; i < faces.capacity(); i++) {
            Rect r = faces.get(i);
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            cvRectangle(image, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.RED, 1, CV_AA, 0);
        }

        changedImg = iplImageToBufImage(image.clone());
        return total;
    }

    static int findLines(BufferedImage bufImage) {
        int total=0;
        //cvHoughLines2();

        //image = toIplImage(bufImage);
        //grayImage = IplImage.create(bufImage.getWidth(), bufImage.getHeight(), IPL_DEPTH_8U, 1);
        //cvCvtColor(image, grayImage, CV_BGR2GRAY);
        
        IplImage src = toIplImage(bufImage);
        IplImage dst;
        IplImage colorDst;

        CvMemStorage storage = cvCreateMemStorage(0);
        CvSeq lines;

        dst = cvCreateImage(cvGetSize(src), src.depth(), 1);
        colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);

        cvCanny(src, dst, 150, 200, 3);
        cvCvtColor(dst, colorDst, CV_GRAY2BGR);

        /*
         * apply the probabilistic hough transform
         * which returns for each line deteced two points ((x1, y1); (x2,y2))
         * defining the detected segment
         */
        lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 150, 50, 3, 0, CV_PI);

        for (int i = 0; i < lines.total(); i++) {
            // Based on JavaCPP, the equivalent of the C code:
            // CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
            // CvPoint first=line[0], second=line[1]
            // is:
            Pointer line = cvGetSeqElem(lines, i);
            CvPoint pt1 = new CvPoint(line).position(0);
            CvPoint pt2 = new CvPoint(line).position(1);

            //Utils.print("Line spotted: ");
            //Utils.print("\t pt1: " + pt1);
            //Utils.print("\t pt2: " + pt2);
            cvLine(src, pt1, pt2, CV_RGB(255, 0, 0), 1, CV_AA, 0); // draw the segment on the image
        }

        changedImg = iplImageToBufImage(src.clone());
        total = lines.total();
        return total;
    }
    
    /*static public BufferedImage test(BufferedImage bufImage) {
        IplImage src1 = toIplImage(bufImage);
        src1 = sharpen(src1);
        BufferedImage tmp = iplImageToBufImage(src1.clone());
        changedImg = tmp;
        currentImg = bufImage;
        return tmp;
    }*/
    /*static IplImage sharpen(IplImage image){
        IplImage dst;
        Pointer pntr = new Pointer();
        dst = cvCreateImage(cvGetSize(image), image.depth(), 3);
        double kernel[] = {-0.1,-0.1,-0.1, -0.1,2,-0.1, -0.1,-0.1,-0.1};
        CvMat kernel_matrix=cvMat(3,3,CV_32FC1,pntr);
        kernel_matrix.get(kernel);
        cvFilter2D(image, dst,kernel_matrix,cvPoint(-1,-1));
        return dst;
    }*/
   
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//Класс содержит методы компьютерного зрения для обработки изображений инстаграма (использует java версию opencv)

package antoshk.instagram.compVision;
import static antoshk.live_belarus.Utils.print;
import java.awt.image.*;
import java.awt.Rectangle;
import java.io.File;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;



/**
 *
 * @author User
 */
public class CVProc {
    //Инициализация библиотеки OpenCV
    static public void init() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    //Конвертер
    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
    
    //Конвертер
    public static BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg){
        if ( matrix != null ) { 
            int cols = matrix.cols();  
            int rows = matrix.rows();  
            int elemSize = (int)matrix.elemSize();  
            byte[] data = new byte[cols * rows * elemSize];  
            int type;  
            matrix.get(0, 0, data);  
            switch (matrix.channels()) {  
            case 1:  
                type = BufferedImage.TYPE_BYTE_GRAY;  
                break;  
            case 3:  
                type = BufferedImage.TYPE_3BYTE_BGR;  
                // bgr to rgb  
                byte b;  
                for(int i=0; i<data.length; i=i+3) {  
                    b = data[i];  
                    data[i] = data[i+2];  
                    data[i+2] = b;  
                }  
                break;  
            default:  
                return null;  
            }  

            // Reuse existing BufferedImage if possible
            if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
                bimg = new BufferedImage(cols, rows, type);
            }        
            bimg.getRaster().setDataElements(0, 0, cols, rows, data);
        } else { // mat was null
            bimg = null;
        }
        return bimg;  
    } 

    //Получение абсолютного пути для файлов ресуров
    public static String getAbsoluteFilepathFromRes(String path){
        String locPath = CVProc.class.getResource(path).getPath();
        File file = new File(locPath);
        return file.getAbsolutePath();
    }

    //Поиск элементов на изображении при помощи обучения
    private static MatOfRect cascadeFindElements(Mat image, String filename) {
      
        MatOfRect elements = new MatOfRect();

        String classifierName;
        classifierName = getAbsoluteFilepathFromRes("haarFiles/"+filename+".xml");
        
        CascadeClassifier faceDetector = new CascadeClassifier(classifierName);

        faceDetector.detectMultiScale(image, elements);

        return elements;
    }
    
    //Поиск лица на изображении опираясь на несколько дампов обучения, их комбинации, в сочетании с поиском изображений глаз внутри возможных областей с лицами
    public static boolean softDetectFace(ImageWrap imageWrap, double minAllowedFaceSqare){
        Mat srcImage = bufferedImageToMat(imageWrap.image);

        
        
        double allowedSqare = srcImage.height() * srcImage.width() * minAllowedFaceSqare / 100;
        
        MatOfRect faces[] = new MatOfRect[4];
        
        faces[0] = cascadeFindElements(srcImage,"haarcascade_frontalface_alt");
            print("Найдено _alt: " + faces[0].total());
        faces[1] = cascadeFindElements(srcImage,"haarcascade_frontalface_alt2");
            print("Найдено _alt2: " + faces[1].total());
        faces[2] = cascadeFindElements(srcImage,"haarcascade_frontalface_alt_tree");
            print("Найдено _alt_tree: " + faces[2].total());
        faces[3] = cascadeFindElements(srcImage,"haarcascade_profileface");
            print("Найдено profileface: " + faces[3].total());
        
        if(faces[0].total() + faces[1].total() + faces[2].total() + faces[3].total() == 0){
            boolean tmp1 = detectEyes(srcImage, true);
            imageWrap.image = matToBufferedImage(srcImage, null);
            return tmp1;
        } else {
            if (faces[0].total() > 0 && faces[1].total() > 0){
                for (Rect rect1 : faces[0].toArray())
                    for (Rect rect2 : faces[1].toArray())
                        if (isRectsSimilar(rect1,rect2)) 
                            if(Math.max(rect1.area(), rect2.area()) > allowedSqare){
                                Imgproc.rectangle(srcImage, new Point(rect1.x, rect1.y), new Point(rect1.x
                                    + rect1.width, rect1.y + rect1.height), new Scalar(255,255,0));
                                Imgproc.rectangle(srcImage, new Point(rect2.x, rect2.y), new Point(rect2.x
                                    + rect2.width, rect2.y + rect2.height), new Scalar(0,255,255));
                                imageWrap.image = matToBufferedImage(srcImage, null);
                                print("Лицо найдено: альт и альт2 указывают на одну область");
                                return true;
                            } else {
                                Imgproc.rectangle(srcImage, new Point(rect1.x, rect1.y), new Point(rect1.x
                                    + rect1.width, rect1.y + rect1.height), new Scalar(255,0,0));
                                imageWrap.image = matToBufferedImage(srcImage, null);
                                print("Лицо найдено, но занимает меньше " + minAllowedFaceSqare + "% поверхности");
                            }                    
            }
            if (faces[3].total() > 0){
                for (Rect rect1 : faces[3].toArray())
                    Imgproc.rectangle(srcImage, new Point(rect1.x, rect1.y), new Point(rect1.x
                                    + rect1.width, rect1.y + rect1.height), new Scalar(200,200,200));
                print("Поиск профиля почти не ошибается!");
                imageWrap.image = matToBufferedImage(srcImage, null);
                return true;
                
            }
            if (faces[0].total() > 0){
                if(detectEyesInSqare(faces[0], srcImage, allowedSqare, imageWrap)){
                    print("Лицо найдено: в области альт найден глаз");
                    return true;
                }
            }
            if (faces[1].total() > 0){
                if(detectEyesInSqare(faces[1], srcImage, allowedSqare, imageWrap)){
                    print("Лицо найдено: в области альт2 найден глаз");
                    return true;
                }
            }            
            if (faces[2].total() > 0){
                if(detectEyesInSqare(faces[2], srcImage, allowedSqare, imageWrap)){
                    print("Лицо найдено: в области альт трии найден глаз");
                    return true;
                }
            }            
            return false;
        }
    }
    
    //Поиск изображений глаз внутри определённой зоны на изображении
    private static boolean detectEyesInSqare(MatOfRect faces, Mat srcImage, double allowedSqare, ImageWrap imageWrap){
        for (Rect rect : faces.toArray()) {
            if (detectEyes(srcImage.submat(rect), false)){
                if(rect.area() > allowedSqare){
                    Imgproc.rectangle(srcImage, new Point(rect.x, rect.y), new Point(rect.x
                                + rect.width, rect.y + rect.height), new Scalar(255,255,0));
                    imageWrap.image = matToBufferedImage(srcImage, null);
                    return true;
                }else{
                    Imgproc.rectangle(srcImage, new Point(rect.x, rect.y), new Point(rect.x
                        + rect.width, rect.y + rect.height), new Scalar(255,0,0));
                    imageWrap.image = matToBufferedImage(srcImage, null);
                    print("Лицо найдено, но занимает меньше минимальной разрешённой поверхности");
                }
            } else {
                //Если изображение глаз не найдено в указанной зоне, поиск по всему изображению, просто для оценки, как часто он ошибается, и принимает за глаз всякую ерунду.
                detectEyes(srcImage, false);
                Imgproc.rectangle(srcImage, new Point(rect.x, rect.y), new Point(rect.x
                        + rect.width, rect.y + rect.height), new Scalar(0,0,255));
                imageWrap.image = matToBufferedImage(srcImage, null);
            }        
        } 
        return false;
    }
    
    //Возвращает true если меньший прямоугольник более чем на половину находится внутри большего
    private static boolean isRectsSimilar(Rect first, Rect second){
        Rectangle fRect, sRect, intersect;
        int fSqare, sSqare, iSqare, avgSqare;
        
        fRect = new Rectangle(first.x, first.y, first.width, first.height);
        sRect = new Rectangle(second.x, second.y, second.width, second.height);
                
        if (fRect.intersects(sRect))
            intersect = fRect.intersection(sRect);
        else {
            print("Прямоугольники не имеют пересечений");
            return false;
        }
            
        
        fSqare = fRect.width * fRect.height;
        sSqare = sRect.width * sRect.height;
        iSqare = intersect.width * intersect.height;
        avgSqare = (fSqare + sSqare)/2;
        
        if(!(Math.min(fSqare, sSqare) - iSqare < Math.min(fSqare, sSqare)/2)){
            print("Прямоугольники имеют слишком маленькую область пересечения");
        }
        return Math.min(fSqare, sSqare) - iSqare < Math.min(fSqare, sSqare)/2; //Вернуть "да" если меньший прямоугольник более чем на половину находится внутри большего 
    }
    
    //Ищет изображение глаза
    private static boolean detectEyes(Mat image, boolean analyse){
        MatOfRect eyes[] = new MatOfRect[2];
        
        eyes[0] = cascadeFindElements(image,"haarcascade_eye");
            print("Найдено _eye: " + eyes[0].total());
        eyes[1] = cascadeFindElements(image,"haarcascade_eye_tree_eyeglasses");
            print("Найдено _eye_tree_eyeglasses: " + eyes[1].total());
        
        if (analyse){
            if (eyes[0].total() > 0 && eyes[1].total() > 0){
                for (Rect rect1 : eyes[0].toArray()){
                    for (Rect rect2 : eyes[1].toArray()){
                        if (isRectsSimilar(rect1,rect2)){
                            Imgproc.rectangle(image, new Point(rect1.x, rect1.y), new Point(rect1.x
                                + rect1.width, rect1.y + rect1.height), new Scalar(100,200,0));
                            Imgproc.rectangle(image, new Point(rect2.x, rect2.y), new Point(rect2.x
                                + rect2.width, rect2.y + rect2.height), new Scalar(0,100,200));
                            return true;
                        } else {
                            Imgproc.rectangle(image, new Point(rect1.x, rect1.y), new Point(rect1.x
                                + rect1.width, rect1.y + rect1.height), new Scalar(0,0,0));
                            Imgproc.rectangle(image, new Point(rect2.x, rect2.y), new Point(rect2.x
                                + rect2.width, rect2.y + rect2.height), new Scalar(0,0,0));
                            print("Глаза найдены в разных местах");
                        }
                    }
                }             
                return false;
            }else
                print("Глаз не найдено вообще");
                return false;
        }else{
            //return eyes[0].total() + eyes[1].total() > 0;
            if (eyes[0].total() + eyes[1].total() > 0){
                for (Rect rect1 : eyes[0].toArray())
                    Imgproc.rectangle(image, new Point(rect1.x, rect1.y), new Point(rect1.x
                                    + rect1.width, rect1.y + rect1.height), new Scalar(100,200,0)); 
                for (Rect rect2 : eyes[1].toArray())
                    Imgproc.rectangle(image, new Point(rect2.x, rect2.y), new Point(rect2.x
                                + rect2.width, rect2.y + rect2.height), new Scalar(0,100,200));
                return true;
            } else 
                return false;
        }
    }
    
    //Ищет прямые линии на изображении (заготовка для определения архитектурных элементов)
    public static int findLines(ImageWrap imageWrap) {
        int total=0;
        //cvHoughLines2();

        //image = toIplImage(bufImage);
        //grayImage = IplImage.create(bufImage.getWidth(), bufImage.getHeight(), IPL_DEPTH_8U, 1);
        //cvCvtColor(image, grayImage, CV_BGR2GRAY);
        
        Mat srcImg = bufferedImageToMat(imageWrap.image);
        Mat grayImg = new Mat();
        Mat colorImg = new Mat();
        Mat edges = new Mat();
        Mat lines = new Mat();

        Imgproc.cvtColor(srcImg, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayImg, grayImg, new Size(3, 3));
        
        Imgproc.Canny(grayImg, edges, 150, 200, 3, false);
        Imgproc.cvtColor(edges, colorImg, Imgproc.COLOR_GRAY2BGR);

        /*
         * apply the probabilistic hough transform
         * which returns for each line deteced two points ((x1, y1); (x2,y2))
         * defining the detected segment
         */
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 150, 50, 3);
        
        for(int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
            Imgproc.line(srcImg, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 255, 0), 2);

        }
        
        imageWrap.image = matToBufferedImage(srcImg, null);
        total = (int)lines.total();
        print("Всего линий найдено: " + total);
        return total;
    }
    
    //Делает изображение резче
    public static Mat sharpen(Mat image){
        Mat dst = new Mat(image.rows(),image.cols(),image.type());
        Mat kernel = new Mat(3,3, CvType.CV_32F){
            {
               put(0,0,-0.1);
               put(0,1,-0.1);
               put(0,2,-0.1);

               put(1,0,-0.1);
               put(1,1,2);
               put(1,2,-0.1);

               put(2,0,-0.1);
               put(2,1,-0.1);
               put(2,2,-0.1);
            }
         };
        Imgproc.filter2D(image, dst, -1, kernel);
        
        return dst;
    }
    
    //Ищет вхождение второго изображения в первое. Результат - вероятность от 0 до 1
    public static double compareMatImages(Mat img, Mat templ, int match_method) {
        //print("\nRunning Template Matching");     

        // / Create the result matrix
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // / Do the Matching
        Imgproc.matchTemplate(img, templ, result, match_method);

        // / Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF
                || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
            return mmr.minVal;
            //print(mmr.minVal);
        } else {
            matchLoc = mmr.maxLoc;
            //print(mmr.maxVal);
            return mmr.maxVal;
        }

        /*// / Show me what you got
        Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

        // Save the visualized detection.
        print("Writing " + outFile);
        Highgui.imwrite(outFile, img);*/

    }
    
    //По сути обёртка для предыдущего метода. Загружает изображения, если разница отношения сторон изображений меньше 0.5, то большее изображение приводится по размерам к меньшему
    //Если нет, то обеспечивается, чтобы поиск был в большем изображении по меньшему
    public static double compareImages(String img1Url, String img2Url){
        Mat img1 = bufferedImageToMat(ImageLdrWrtr.loadImage(img1Url));
        Mat img2 = bufferedImageToMat(ImageLdrWrtr.loadImage(img2Url));
        
        double propDiff = Math.abs(img1.width()/img1.height() - img2.width()/img2.height());
        //print(propDiff);
        if (propDiff < 0.5) {
            Mat toScale, toCompare;
            if (img1.width()> img2.width()) {
                toScale = img1;
                toCompare = img2;
            }else{
                toScale = img2;
                toCompare = img1;
            }
            Mat img3 = new Mat();
            Imgproc.resize(toScale, img3, (new Size(toCompare.width(), toCompare.height())));
            return compareMatImages(img3, toCompare, Imgproc.TM_CCOEFF_NORMED);
        }
        
        if(img1.width() >= img2.width() && img1.height() >= img2.height()) return compareMatImages(img1, img2, Imgproc.TM_CCOEFF_NORMED);
        if(img1.width() <= img2.width() && img1.height() <= img2.height()) return compareMatImages(img2, img1, Imgproc.TM_CCOEFF_NORMED);
        else {
            Mat img3 = new Mat();
            Imgproc.resize(img1, img3, (new Size(Math.max(img1.width(), img2.width()), Math.max(img1.height(), img2.height()))));
            return compareMatImages(img3, img2, Imgproc.TM_CCOEFF_NORMED);
        }
    }
    


}



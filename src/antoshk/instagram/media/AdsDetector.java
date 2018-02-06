/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.media;
import antoshk.instagram.compVision.CVProc;
import antoshk.instagram.compVision.ImageLdrWrtr;
//import static antoshk.instagram.CVProc.bufferedImageToMat;
import antoshk.instagram.entity.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import antoshk.instagram.tag.TagProc;
import static antoshk.live_belarus.Utils.print;

/**
 *
 * @author User
 */
public class AdsDetector {
    MediaList mediaList;
    List<String> AdsLowSignals;
    List<String> AdsHighSignals;
    List<String> AdsTagFlags;
    
    public AdsDetector(){
        mediaList = new MediaList();
        AdsLowSignals = Arrays.asList("хочу", "конкурс", "розыгрыш", "сделать репост", "победител", "подписаться", "паблик", "клуб", "club");
        
        AdsHighSignals = Arrays.asList("giveaway", "случайных чисел", "быть нашим подписчиком", 
                "быть подписчиком", "вход по фамилии");
        
        AdsTagFlags = Arrays.asList("giveaway", "конкурс", "розыгрыш", "give_away", "shoe", "bags", "nail", "маникюр", 
                "beauty", "sushi", "brand", "абонемент" , "подарок", "суши", "BHC", "doragabogata");
        
    }
    public AdsDetector(MediaList ml){
        this();
        mediaList.addAll(ml);
        
    }
    public void setMediaList(MediaList ml){
        mediaList.clear();
        mediaList.addAll(ml);
    }
    
    //Метод проверяет, не состоит ли набор из одинаковых изображений (что часто встречается среди всяких конкурсов)
    public boolean checkImages(){
        double result;
        int trueSeries;
        int falseSeries;
        int trueLimit = (int) Math.round(mediaList.size()*0.6);
        int falseLimit = Math.max((int) Math.round(mediaList.size()*0.3), 1);
        for (int i=0; i<Math.min(falseLimit+2, mediaList.size()); i++){
            falseSeries = 0;
            trueSeries=0;
            for (int k=0; k<mediaList.size(); k++){
                if (i==k) continue;
                print("Сравниваю "+mediaList.get(i).getLink()+" и " + mediaList.get(k).getLink());
                result = CVProc.compareImages(mediaList.get(i).getLink(), mediaList.get(k).getLink());
                print("Результат "+result);
                if (result > .6) trueSeries++;
                else falseSeries++;
                if (falseSeries > falseLimit) break;
                if (trueSeries > trueLimit) return true;
            }
        }
        return false;
    }
    
    //Проверяет описание постов на наличие ключевых слов
    public boolean checkCaption(){
        double captionDetect = detectByCaption(false);
        //более 50% медиа в наборе имеют отношение к розыгрышам
        return (captionDetect > 50);
    }
    
    //Проверяет тэг, на содержание ключевых слов
    public boolean checkTag(String tag){
        List<String> linkedTags = (new TagProc()).getLinkedTags(tag);
        for(String flag: AdsTagFlags){
            if (tag.contains(flag)) return true;
            for(String linkedTag: linkedTags){
                if (linkedTag.contains(flag)) return true;
            }
        }
        return false;
    }
    
    //Проверяет описание постов и возвращает процент от общего количества постов в наборе, в которых найдены ключевые слова
    private double detectByCaption(boolean noDoubt){
        double result=0;
        int doubt = 0;
        int counter;
        int highPriorCounter = 0;
        for (Media media: mediaList){
            counter = countOccurence(media, AdsLowSignals);
            if (counter > 1) result++;
            else if (counter == 1){
                if (noDoubt) result++;
                else doubt++;
            }
            highPriorCounter += countOccurence(media, AdsLowSignals);
            if (highPriorCounter > 1) return 100;
        }
        
        //влияние медиа, по которым есть сомнения. Учитываются в процентном соотношении от половины всех медиа. Если больше половины, то всё равно множитель 1.
        if (!noDoubt){
            doubt = doubt*(Math.min(doubt, mediaList.size()/2)/(mediaList.size()/2));
            result += doubt;
        }
        result = (result/mediaList.size())*100;
        
        return result;        
    }
    
    //Возвращает количество найденных ключевых слов
    private int countOccurence(Media media, List<String> signalList){
        int count = 0;
        for (String signal: signalList){
            if(media.getCaption().toLowerCase().contains(signal)) count++;
        }
        return count;
    }
    
    //Ищет иконку репоста на изображении
    public static boolean containsRepostIcon(String imageURL){
        Mat image = CVProc.bufferedImageToMat(ImageLdrWrtr.loadImage(imageURL));
        Mat icon1 = CVProc.bufferedImageToMat(ImageLdrWrtr.loadImage(CVProc.getAbsoluteFilepathFromRes("media/repostIcons/repost1.jpg")));
        Mat icon2 = CVProc.bufferedImageToMat(ImageLdrWrtr.loadImage(CVProc.getAbsoluteFilepathFromRes("media/repostIcons/repost2.jpg")));
        if (CVProc.compareMatImages(image, icon1, Imgproc.TM_CCOEFF_NORMED)> .85) return true;
        if (CVProc.compareMatImages(image, icon2, Imgproc.TM_CCOEFF_NORMED)> .85) return true; 
        return false;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.media;

import antoshk.instagram.compVision.ImageLdrWrtr;
import antoshk.instagram.dao.DAO;
import antoshk.instagram.dao.UniDAO;
import antoshk.instagram.dao.UserDAO;
import antoshk.instagram.entity.Media;
import antoshk.instagram.entity.User;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author User
 */
public class MediaProc {

    //Возвращает строку-описание к инстаграм-посту (автор, ссылку на него, ссылку на саму публикацию и описание автора)
    public static String getMediaDescr(Media media){
        
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getByInstaId(Long.parseLong(media.getOwnerId()));
        if (user == null) return "Ссылка на пост: https://www.instagram.com/p/"+ media.getCode()+"/ \n";
        StringBuilder result = new StringBuilder("Автор снимка: " + user.getUsername() + " https://www.instagram.com/" + user.getUsername() +"/ \n");
        result.append("Ссылка на пост: https://www.instagram.com/p/").append(media.getCode()).append("/ \n");
        result.append("Описание: ").append(media.getCaption()).append(" \n");
        return result.toString();
    }
    
    //Загружает изображение во временный файл
    public static void loadMedia(Media media){
        try{
            File file = File.createTempFile("instaPhoto", ".jpg");
            BufferedImage image = ImageLdrWrtr.loadImage(media.getLink());
            ImageLdrWrtr.writeImage(file, image);
            media.setFile(file);
        } catch(Exception e){}
        
    }
    
    //Возвращает список всех слов длиннее трёх символов в порядке, начиная с наибольшего количества упоминаний. Слова берутся из описаний к посту
    public static List<String> getKeyWords(MediaList mediaList){
        List<String> keyWords = new ArrayList();
        String[] words;
        String tempWord;
        for (Media media: mediaList){
            if(media.getCaption() != null){
                words = media.getCaption().replaceAll("#", " #").split("[ ,\n:]");
                for(String word : words){
                    if(word.indexOf("#")==0) continue;
                    tempWord = word.replaceAll("'", "&quot;").replaceAll("[^\\w\\n А-Яа-я]+", "").replaceAll("_", "");
                    if (tempWord.length() > 3) keyWords.add(tempWord);
                }
            }
        }
        HashMap<String, Integer> wordMap = new HashMap();
        for(String word : keyWords){
            if(wordMap.get(word) == null){
                wordMap.put(word, 1);
            }else {
                wordMap.put(word, wordMap.get(word)+1);
            }
        }
        keyWords.clear();
        while (wordMap.size()>0){
            int max = 0;
            String wordValue = "";
            for(Map.Entry<String, Integer> word : wordMap.entrySet()){
                if(word.getValue()>max){
                    max = word.getValue();
                    wordValue = word.getKey();
                }
            }
            wordMap.remove(wordValue);
            keyWords.add(wordValue);
        }
        return keyWords;
    }
}

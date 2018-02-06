/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//Старый класс
package antoshk.instagram.media;
import antoshk.instagram.compVision.CVProc;
import antoshk.instagram.compVision.ImageLdrWrtr;
import antoshk.instagram.compVision.ImageWrap;

import antoshk.instagram.entity.Location;
import antoshk.instagram.entity.Media;
import antoshk.live_belarus.DataBase;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.awt.image.*;
import java.util.Date;
import antoshk.live_belarus.EndPoints;
import antoshk.live_belarus.Utils;
import static antoshk.live_belarus.Utils.print;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.sql.ResultSet;

/**
 *
 * @author User
 */
public class MediaSetOld {
    
    Location locs;
    //int searchDepth=100;
    List<Media> mediaStorage;
    Utils utils = new Utils();
    
    public MediaSetOld(){
        mediaStorage = new ArrayList();
    }
    
    public static MediaSetOld getMediaSetByLatLng(String Lat, String Lng){
        MediaSetOld m = new MediaSetOld();
        m.locs = Location.getLocationObject(Lat, Lng);
        for(int i=0; i<m.locs.size(); i++){
            m.loadMediaPortionByLocId(m.locs.getLocId(i), null);  
        }
        return m;
    }
    public static MediaSetOld getMediaSetByLocId(String locId, int searchDepth){
        MediaSetOld m = new MediaSetOld();
        String maxId;
        maxId = m.loadMediaPortionByLocId(locId, null);
        for(;;){
            if (maxId != null && m.mediaStorage.size()<searchDepth){
                maxId = m.loadMediaPortionByLocId(locId, maxId);
            }else{
                break;
            }
        }
        print("Total media count: " + m.mediaStorage.size()); 
        return m;
    }
    
    public static MediaSetOld getMediaSetByUsername(String username, int searchDepth, long timeAgo){
        MediaSetOld m = new MediaSetOld();
        String maxId;
        maxId = m.loadMediaPortionByUsername(username, null);
        for(;;){
            if (maxId != null && m.mediaStorage.size()<searchDepth){
                if (timeAgo > 0){
                    Media media = m.mediaStorage.get(m.mediaStorage.size()-1);
                    long mediaDate;
                    mediaDate = Long.parseLong(media.getDate());
                    
                    if ((new Date()).getTime() - mediaDate*1000 < timeAgo*1000)
                        maxId = m.loadMediaPortionByUsername(username, maxId);
                    else
                        break;
 
                } else {
                    maxId = m.loadMediaPortionByUsername(username, maxId);
                }
            }else{
                break;
            }
        }
        if (timeAgo > 0){
            Iterator<Media> iter = m.mediaStorage.iterator();
            while (iter.hasNext()) {
                Media media = iter.next();

                long mediaDate;
                mediaDate = Long.parseLong(media.getDate());
                if ((new Date()).getTime() - mediaDate*1000 > timeAgo*1000)
                    iter.remove();
            }
        }
        print("Total media count: " + m.mediaStorage.size());         
        return m;
    }
    
    protected static MediaSetOld filterUserMediaSetByTag(String username, String tag, long timeAgo){
        MediaSetOld m;
        if (timeAgo>0) m = getMediaSetByUsername(username, 50, timeAgo);
        else m = getMediaSetByUsername(username, 50, 60*60*24);
        
        Iterator<Media> iter = m.mediaStorage.iterator();
        while (iter.hasNext()) {
            Media media = iter.next();

            long mediaDate;
            mediaDate = Long.parseLong(media.getDate());
            if (media.getCaption() == null || !media.getCaption().contains(tag)) iter.remove();
            else if (media.getCaption() != null && (media.getCaption().contains("oscow") || media.getCaption().contains("осква"))) iter.remove();
        }
        return m;
    }
    
    public static MediaSetOld getMediaSetByTag(String tag, long timeAgo){
        MediaSetOld m = new MediaSetOld();
        ResultSet rs;
        rs = DataBase.exeSelectQuery("SELECT id, user_name, tag_name FROM insta_user_tags WHERE tag_name='"+tag+"'");
        try{
            while(rs.next()){ 
                
                m.addMedias(MediaSetOld.filterUserMediaSetByTag(rs.getString("user_name"), tag, timeAgo));
            }  
        } catch(Exception e){
            print("Ошибка при выборке изображений по тэгу: " + e.getMessage());
        }
        return m;
    }
    
    public BufferedImage getImage(int i){
        return(mediaStorage.get(i).getCascadedImage());
    }
    public int getCount(){
        return(mediaStorage.size());
    }
    public String getDate(int i){
        return(mediaStorage.get(i).getDate());
    }
    
    public void addMedias(MediaSetOld m){
        mediaStorage.addAll(m.mediaStorage);
    }
    public void addMedia(Media m){
        mediaStorage.add(m);
    }
    
    public String getMostLikedLink(){
        MediaSetOld minskSet = new MediaSetOld();
        MediaSetOld toCheck;
        for (Media media: mediaStorage){
            if (media.getCaption().contains("insk") || media.getCaption().contains("инск")) minskSet.mediaStorage.add(media);
        }
        if (minskSet.mediaStorage.size() > 0) toCheck = minskSet;
        else toCheck = this;
        
        Media m = mediaStorage.get(0);
        for (Media media: toCheck.mediaStorage){
            if (media.getLikesCount() > m.getLikesCount()) m=media;
        }
        return "https://www.instagram.com/p/"+m.getCode()+"/";
    }
    public String getMostCommentedLink(){
        MediaSetOld minskSet = new MediaSetOld();
        MediaSetOld toCheck;
        for (Media media: mediaStorage){
            if (media.getCaption().contains("insk") || media.getCaption().contains("инск")) minskSet.mediaStorage.add(media);
        }
        if (minskSet.mediaStorage.size() > 0) toCheck = minskSet;
        else toCheck = this;
        
        Media m = mediaStorage.get(0);
        for (Media media: toCheck.mediaStorage){
            if (media.getCommentsCount() > m.getCommentsCount()) m=media;
        }
        return "https://www.instagram.com/p/"+m.getCode()+"/";
    }    

    public void checkMedias(){
        CVProc.init();
        
        ImageWrap imageWrap;
        for (int i=0; i<mediaStorage.size(); i++){
            print("Проверяю изображение "+ i +": " + mediaStorage.get(i).getLink());
            imageWrap = new ImageWrap();
            imageWrap.image = ImageLdrWrtr.loadImage(mediaStorage.get(i).getLink());
            if(CVProc.softDetectFace(imageWrap, 0.5))
                print("На изображении "+ i +" найдено одно или более лиц");
            else
                print("На изображении "+ i +" лиц не найдено");

            mediaStorage.get(i).setCascadedImage(imageWrap.image);
            
            if (mediaStorage.get(i).getCascadedImage() == null){
                mediaStorage.remove(i);
                i--;
            }
            //if (media.cascadedImage == null) print("null");
            //if (i>2) break;
        }

    }
    
    
    protected String loadMediaPortionByLocId(String locId, String maxId){
        String urlString = EndPoints.MediaByLoc; 
        urlString = urlString.replace("[loc_id]", locId);
        if (maxId != null) urlString += "&max_id="+maxId;

        return parseMediaJSON(utils.sendRequest(urlString), "location");
    }
    
    protected String parseMediaJSON(String jsonString, String source){
        JsonParser parser = new JsonParser();
        if (jsonString == null) return null;
        JsonArray data = null;
        try{
            data = parser.parse(jsonString).getAsJsonObject().getAsJsonObject(source).getAsJsonObject("media").getAsJsonArray("nodes");
        }catch(Exception e){
            print("При попытке парсинга джсона '" + jsonString + "' возникла ошибка!");
            return null;
        }
            
        if (source.equals("user")){
            boolean isPrivate = parser.parse(jsonString).getAsJsonObject().getAsJsonObject(source).get("is_private").getAsBoolean();
            if (isPrivate) return null;
        }
        if (data.size() == 0) return null;
        Media media;
        
        for (int i=0; i<data.size(); i++){
            media = new Media();
            JsonObject jsonMedia = data.get(i).getAsJsonObject();
            if (jsonMedia.has("caption")){
                media.setCaption(jsonMedia.get("caption").getAsString());
            }
            media.setId(jsonMedia.get("id").getAsString());
            media.setCode(jsonMedia.get("code").getAsString());
            media.setCommentsCount(jsonMedia.getAsJsonObject("comments").get("count").getAsInt());
            media.setLikesCount(jsonMedia.getAsJsonObject("likes").get("count").getAsInt());
            media.setLink(jsonMedia.get("display_src").getAsString());
            
            int qIndex = media.getLink().indexOf('?');
            if (qIndex > 0) media.setLink(media.getLink().substring(0, qIndex));
            
            media.setDate(jsonMedia.get("date").getAsString());
            media.setOwnerId(jsonMedia.getAsJsonObject("owner").get("id").getAsString());
            media.setLocId(jsonMedia.get("id").getAsString());
            media.setApplyable(false);
            
            mediaStorage.add(media);
        }
        
        return parser.parse(jsonString).getAsJsonObject().getAsJsonObject(source).getAsJsonObject("media").getAsJsonObject("page_info").get("end_cursor").getAsString();
    }
    
    protected String loadMediaPortionByUsername(String username, String maxId){
        String urlString = EndPoints.MediaByUsername; 
        urlString = urlString.replace("[username]", username);
        if (maxId != null) urlString += "&max_id="+maxId;

        return parseMediaJSON(utils.sendRequest(urlString), "user");
    }
    
    public ArrayList extractTags(){
        ArrayList<String> tags = new ArrayList();
        String[] words;
        String tmpWord=null;
        for(Media media : mediaStorage){
            if(media.getCaption() != null){
                words = media.getCaption().replaceAll("#", " #").split("[ ,\n:]");
                for(String word : words){
                    if(word.indexOf("#")==0){
                        tmpWord = word.replaceAll("'", "&quot;").replaceAll("[^\\w\\n \\:\\.\\,\\-\\&\\;\\#А-Яа-я]+", "");
                        tags.add(tmpWord);   
                    }
                }
            }
        }
       return tags; 
    }

    public HashMap<String, Boolean> isMedaisFrom(HashMap<String, String[]> searchQuery, int countToCheck){
        int countryCaptionRef = 0;
        int countryLocRef = 0;
        int cityCaptionRef = 0;
        int cityLocRef = 0;
        
        HashMap<String, Integer> params = new HashMap();
        for(Map.Entry<String, String[]> searchOption : searchQuery.entrySet()){
            params.put(searchOption.getKey()+"CaptionRef", 0);
            params.put(searchOption.getKey()+"LocRef", 0);
        }
        
        int mediaWithLoc = 0;
        int mediaTotal = 0;
        boolean hasFound = false;
        HashMap<String, Boolean> result = new HashMap();

        for(Media media : mediaStorage){
            if(mediaTotal>countToCheck){
                for(Map.Entry<String, String[]> searchOption : searchQuery.entrySet()){
                    if(params.get(searchOption.getKey()+"CaptionRef") + params.get(searchOption.getKey()+"LocRef")/(double)mediaTotal > 0.2) result.put(searchOption.getKey(), true);
                    if(params.get(searchOption.getKey()+"LocRef")/(double)mediaWithLoc > 0.2) result.put(searchOption.getKey(), true);
                }
                /*if (cityCaptionRef + cityLocRef/(double)mediaTotal > 0.2) result.put("locality", true);
                if (cityLocRef/(double)mediaWithLoc > 0.2) result.put("locality", true);
                if (countryCaptionRef + countryLocRef/(double)mediaTotal > 0.2) result.put("country", true);
                if (countryLocRef/(double)mediaWithLoc > 0.2) result.put("country", true);*/
                return result;
            }
            
            int locRef=0;
            for(Map.Entry<String, String[]> searchOption : searchQuery.entrySet()){
                if (!searchOption.getKey().equals("belarus") && params.get(searchOption.getKey()+"CaptionRef") + params.get(searchOption.getKey()+"LocRef") > 10 &&
                        params.get("belarusCaptionRef") + params.get("belarusLocRef") > 10){
                    result.put("belarus", true);
                    result.put(searchOption.getKey(), true);
                    return result;
                }
                locRef += params.get(searchOption.getKey()+"LocRef");
            }
            
            if (locRef > 10) result.put("belarus", true);
                       
            if(media.getCaption() != null){
                
                for(Map.Entry<String, String[]> searchOption : searchQuery.entrySet())
                    for (String searchStr : searchOption.getValue())
                        if(media.getCaption().contains(searchStr) || media.getCaption().contains(searchStr.toLowerCase())){
                            params.put(searchOption.getKey()+"CaptionRef", params.get(searchOption.getKey()+"CaptionRef")+1);
                            mediaTotal++;
                            hasFound = true;
                        }
                if (hasFound){
                    hasFound = false;
                    continue;
                }
            }
            String url = EndPoints.MediaByMediaCode;
            url = url.replace("[mediaCode]", media.getCode());
            String json = utils.sendRequest(url);
            JsonParser parser = new JsonParser();
            JsonObject jsonMedia = parser.parse(json).getAsJsonObject().getAsJsonObject("media");
            ArrayList<String> resultLocs;
            
            if (jsonMedia.get("location").isJsonObject()){
                mediaWithLoc++;
                String locId = jsonMedia.getAsJsonObject("location").get("id").getAsString();
                resultLocs = Location.localRevGeocoding(locId);
                for(String resultLoc : resultLocs){
                    params.put(resultLoc+"LocRef", params.get(resultLoc+"LocRef")+1);
                } 
            }
            mediaTotal++;
            
        } 
        return result;  
    }




    
}




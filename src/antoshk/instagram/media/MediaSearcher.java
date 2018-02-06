package antoshk.instagram.media;



import antoshk.instagram.entity.Location;
import antoshk.instagram.entity.Media;
import antoshk.instagram.entity.TagToUser;
import antoshk.live_belarus.EndPoints;
import antoshk.proxy.ProxyList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Date;
import java.util.List;
import antoshk.instagram.dao.TagToUserDAO;
import antoshk.instagram.filter.*;
import antoshk.live_belarus.Utils;
import static antoshk.live_belarus.Utils.print;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class MediaSearcher {


    // Порциями (постранично) получает медиа пользователя, до тех пор, пока они не закончатся, либо наберётся достаточное количество, либо 
    // будет достигнут предел по времени публикации и все оставшиеся записи будут сделаны раньше нужного
    public static MediaList getMediaListByUsername(String username, int mediaCount, long timeAgo){
        MediaList mediaList = new MediaList();
        String maxId;
        maxId = loadMediaPortionByUsername(username, null, mediaList);
        for(;;){
            if (maxId != null && mediaList.size()<mediaCount){
                if (timeAgo > 0){
                    Media media = mediaList.get(mediaList.size()-1);
                    long mediaDate;
                    mediaDate = Long.parseLong(media.getDate());
                    
                    if ((new Date()).getTime() - mediaDate*1000 < timeAgo*1000)
                        maxId = loadMediaPortionByUsername(username, maxId, mediaList);
                    else
                        break;
 
                } else {
                    maxId = loadMediaPortionByUsername(username, maxId, mediaList);
                }
            }else{
                break;
            }
        }
        if (timeAgo > 0)
            mediaList.filter(new filterMediaByTime(timeAgo));

        //print("Total media count: " + mediaList.size());         
        return mediaList;
    }
    
    //По заданным координатам получает список инстаграм-локаций, загружает по одной порции медиа из каждой локации
    public static MediaList getMediaListByLatLng(String Lat, String Lng){
        MediaList mediaList = new MediaList();
        Location locs = Location.getLocationObject(Lat, Lng);
        for(int i=0; i<locs.size(); i++){
            loadMediaPortionByLocId(locs.getLocId(i), null, mediaList);  
        }
        return mediaList;
    }
    //По заданной инстаграм-локации загружает постранично медиа пока они не закончатся, либо не будет достигнут предел количества медиа
    public static MediaList getMediaListByLocId(String locId, int mediaCount){
        MediaList mediaList = new MediaList();
        String maxId;
        maxId = loadMediaPortionByLocId(locId, null, mediaList);
        for(;;){
            if (maxId != null && mediaList.size()<mediaCount){
                maxId = loadMediaPortionByLocId(locId, maxId, mediaList);
            }else{
                break;
            }
        }
        print("Total media count: " + mediaList.size()); 
        return mediaList;
    }    
    
    //Находит в базе данных всех пользователей, кто при последней проверке упоминал в своих постах искомый тэг, затем загружает эти изображения
    public static MediaList getMediaListByTag(String tag, long timeAgo){
        MediaList mediaList = new MediaList();
        TagToUserDAO ttuDAO = new TagToUserDAO();
        List<TagToUser> ttuList = ttuDAO.getManyByName(tag);

        if (timeAgo == 0) timeAgo = 60*60*24;

        for(TagToUser ttu: ttuList){
            MediaList tempList = getMediaListByUsername(ttu.getUserName(), 50, timeAgo);
            tempList.filter(new filterMediaByTag(tag));
            tempList.filter(new filterMediaByMoscow());
            mediaList.addAll(tempList);
        }
        return mediaList;
    }
    
    //Генерирует ссылку-запрос к серверу инстаграм, основанный на ИД инстаграм-локации, отправляет запрос, получает результат запроса и возвращает результат работы парсера
    protected static String loadMediaPortionByLocId(String locId, String maxId, List<Media> storage){
        String urlString = EndPoints.MediaByLoc; 
        urlString = urlString.replace("[loc_id]", locId);
        if (maxId != null) urlString += "&max_id="+maxId;

        return parseMediaJSON(Utils.sendRequest(urlString), "location", storage);
    }
    
    //Генерирует ссылку-запрос к серверу инстаграм, основанный на имени пользователя, отправляет запрос, получает результат запроса и возвращает результат работы парсера
    protected static String loadMediaPortionByUsername(String username, String maxId, List<Media> storage){
        String urlString = EndPoints.MediaByUsername; 
        urlString = urlString.replace("[username]", username);
        if (maxId != null) urlString += "&max_id="+maxId;

        return parseMediaJSON(Utils.sendRequest(urlString), "user", storage);
    }
    
    //Парсит JSON-ответ от сервера инстаграм, складывает результат в storage и возвращает maxId, по которому можно загрузить средующую порцию
    protected static String parseMediaJSON(String jsonString, String source, List<Media> storage){
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
            
            storage.add(media);
        }
        
        return parser.parse(jsonString).getAsJsonObject().getAsJsonObject(source).getAsJsonObject("media").getAsJsonObject("page_info").get("end_cursor").getAsString();
    }


    
}

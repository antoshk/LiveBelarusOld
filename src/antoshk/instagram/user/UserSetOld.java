/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.user;

import antoshk.instagram.States;

import antoshk.instagram.media.MediaSetOld;
import antoshk.instagram.entity.User;
import antoshk.live_belarus.DataBase;
import antoshk.live_belarus.EndPoints;
import antoshk.live_belarus.Utils;
import static antoshk.live_belarus.Utils.print;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author User
 */
public class UserSetOld {
    ArrayList<User> usersStorage = new ArrayList();
    
    public int getUserCount(){
        return usersStorage.size();
    }
    public long getDBId(int arrayId){
        return usersStorage.get(arrayId).getId();
    }
    public long getDBId(String username){
        for(User user : usersStorage){
            if (user.getUsername().equals(username)) return user.getId();
        }
        return 0;
    }    
    public String getUsername(int arrayId){
        return usersStorage.get(arrayId).getUsername();
    }
    public String getUserId(int arrayId){
        return usersStorage.get(arrayId).getInstaId();
    }
    public String getUserId(String username){
        for(User user : usersStorage){
            if (user.getUsername().equals(username)) return user.getInstaId();
        }
        return null;
    }

    
    public String getUserProrerty(int userId, String propertyName){
        switch(propertyName){
            case "userId":
                return usersStorage.get(userId).getInstaId();
            case "DBId":
                return String.valueOf(usersStorage.get(userId).getId());
            case "username":
                return usersStorage.get(userId).getUsername();
            case "biography":
                return usersStorage.get(userId).getBiography();
            case "followed_by":
                return usersStorage.get(userId).getFollowed_by()+"";
            case "follows":
                return usersStorage.get(userId).getFollows()+"";
        }
        return null;
    }
    
    public static UserSetOld getUserSet(int startPos){
        UserSetOld users = new UserSetOld();
        ResultSet rs;
        
        if (startPos != 0)
            rs = DataBase.exeSelectQuery("SELECT id, insta_id, username FROM insta_users_ext where id >= " + startPos + " ORDER BY id");
        else
            rs = DataBase.exeSelectQuery("SELECT id, insta_id, username FROM insta_users_ext ORDER BY id");

        try{
            while(rs.next()){
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setInstaId(rs.getString("insta_id"));
                user.setId(rs.getInt("id"));
                users.usersStorage.add(user);
            }
        } catch(Exception e){
            print("Ошибка при извлечении имён пользователей: " + e.getMessage());
        }        
        return users;
    }    
    
    User getUserInfo(String username){
        String url = EndPoints.MediaByUsername;
        long mediaDate, time;
        time = (new Date()).getTime();
        
        url = url.replace("[username]", username);
        Utils utils = new Utils();
        String jsonString = utils.sendRequest(url);
        if(jsonString == null){
            User user = new User();
            user.setUsername(username);
            user.setNotExists(true);
            return user;
        }
        
        JsonParser parser = new JsonParser();
        User user = new User();
        JsonObject data = parser.parse(jsonString).getAsJsonObject().getAsJsonObject("user");
        user.setInstaId(data.get("id").getAsString());
        user.setUsername(data.get("username").getAsString());
        user.setFollows(data.getAsJsonObject("follows").get("count").getAsInt());
        user.setFollowed_by(data.getAsJsonObject("followed_by").get("count").getAsInt());
        user.setMediaCount(data.getAsJsonObject("media").get("count").getAsInt());
        user.setIsPrivate(data.get("is_private").getAsBoolean());
        if (user.isIsPrivate()) return user;
        
        mediaDate = data.getAsJsonObject("media").getAsJsonArray("nodes").get(0).getAsJsonObject().get("date").getAsLong();
        
        //print(String.valueOf(time)+"-"+String.valueOf(mediaDate*1000)+"/"+String.valueOf(60*60*24*1000));
        user.setDaysFromLastPost((int)((time - mediaDate*1000)/(60*60*24*1000)));
        
        
        user.setNotExists(false);
        
        
        
        if(data.get("biography").isJsonPrimitive()){
            user.setBiography(data.get("biography").getAsString());
            user.setBiography(user.getBiography().replaceAll("[^\\w\\n \\:\\.\\,\\-А-Яа-я]+", ""));
        }
        else user.setBiography("");
        
        //user.biography = data.get("biography").getAsString();
        return user;
    } 
    
    
    public static void updateUsers(){

        ResultSet rs;
        rs = DataBase.exeSelectQuery("SELECT id, username FROM insta_users_ext");

        GetUserInfoThread[] getUserInfoThread = new GetUserInfoThread[5];
        
        /*HashMap<String, String[]> query = new HashMap();
        query.put("belarus", new String[]{"Беларус", "Belarus", "Белорус"});
        query.put("minsk", new String[]{"Минск", "Minsk"});
        query.put("mogilev", new String[]{"Могилев", "Могилёв", "Mogilev"});
        query.put("gomel", new String[]{"Гомель", "Gomel"});
        query.put("grodno", new String[]{"Гродн", "Grodn"});
        query.put("vitebsk", new String[]{"Витебск", "Vitebsk"});
        query.put("brest", new String[]{"Брест", "Brest"});*/
        
        boolean thrdNotFound;
        try{
            while(rs.next()){
                thrdNotFound = true;
                while(thrdNotFound){
                    for(int k=0; k<5; k++){
                        if(getUserInfoThread[k] == null || !getUserInfoThread[k].isAlive()){
                            getUserInfoThread[k] = new GetUserInfoThread();
                            getUserInfoThread[k].username = rs.getString("username");
                            getUserInfoThread[k].id = rs.getString("id");
                            //getUserInfoThread[k].query = query;
                            getUserInfoThread[k].start();
                            thrdNotFound = false;
                            break;
                        }
                    }
                }
            }
        } catch(Exception e){
            print("Ошибка: " + e.getMessage());
        }    
        
        
        
    }
    public static void getFollowers(){
        States state = new States();
        UserSetOld users;
        if ("notFinished".equals(state.getState("getFollowersState"))){
            int startFrom = Integer.parseInt(state.getState("getFollowersStartPos"));
            users = getUserSet(startFrom);
        } else {
            users = getUserSet(100);
        }
        state.setState("getFollowersState", "notFinished");
        state.setState("getFollowersStartPos", "0");
        
        GetFollowersThread[] followersThread = new GetFollowersThread[1];
        
        
        boolean thrdNotFound;
        for(int i=0; i<users.getUserCount(); i++){
            thrdNotFound = true;
            while(thrdNotFound){
                for(int k=0; k<1; k++){
                    if(followersThread[k] == null || !followersThread[k].isAlive()){
                        followersThread[k] = new GetFollowersThread();
                        followersThread[k].counter = i;
                        followersThread[k].username = users.getUsername(i);
                        followersThread[k].threadN = k;
                        followersThread[k].start();
                        state.setState("getFollowersStartPos", String.valueOf(users.getDBId(i)));
                        thrdNotFound = false;
                        break;
                    }
                }
            }
        }

        state.setState("getFollowersState", "Finished");
    }
    
    void identUserLocation(User user, HashMap searchReq){
        //States state = new States();
        //User user = getUserInfo(username);

        HashMap<String, Boolean> searchResults = Utils.searcher(user.getBiography(), searchReq);
        for(Map.Entry<String, Boolean> result : searchResults.entrySet()){
            if (result.getValue()){
                print(user.getUsername() + " -(bio)- " + result.getKey());
                if (result.getKey().equals("belarus")) DataBase.exeChangeQuery("UPDATE insta_temp_users_unique SET isBelarus='1' WHERE id = '"+ user.getId() +"'");
                else {
                    DataBase.exeChangeQuery("UPDATE insta_temp_users_unique SET city='" + result.getKey() + "', isBelarus='1' WHERE id = '"+ user.getId() +"'");
                    return;
                }
            }
        }

        MediaSetOld medias = MediaSetOld.getMediaSetByUsername(user.getUsername(), 50, 0);
        searchResults = medias.isMedaisFrom(searchReq, 50);

        for(Map.Entry<String, Boolean> result : searchResults.entrySet()){
            if (result.getValue()){
                print(user.getUsername() + " -(geoloc)- " + result.getKey());
                if (result.getKey().equals("belarus")) DataBase.exeChangeQuery("UPDATE insta_temp_users_unique SET isBelarus='1' WHERE id = '"+ user.getId() +"'");
                else {
                    DataBase.exeChangeQuery("UPDATE insta_temp_users_unique SET city='" + result.getKey() + "', isBelarus='1' WHERE id = '"+ user.getId() +"'");
                }
            }
        }
    }
    
    
    
}



class GetFollowersThread extends Thread{
    String request, username, jsonString, followerUsername;
    int counter, threadN;
    
    @Override
    public void run(){
        Utils utils = new Utils();
        request = EndPoints.FollowersByUsername;
        request = request.replace("[username]", username);
     
        jsonString = utils.sendRequest(request);
        JsonParser parser = new JsonParser();
        JsonArray data = null;
        if (jsonString == null) return;
        
        try{
            data = parser.parse(jsonString).getAsJsonObject().getAsJsonArray("users");
        }catch(Exception e){
            print("При попытке парсинга джсона '" + jsonString + "' возникла ошибка!");
            return;
        }
        
        for (int i=0; i<data.size(); i++){
            JsonObject jsonUser = data.get(i).getAsJsonObject();
            followerUsername = jsonUser.get("username").getAsString();
            DataBase.exeChangeQuery("INSERT INTO insta_temp_users SET username='" + followerUsername + "'");
        }
        print("От пользователя '" + username + "' добавлено " + data.size() + " подписчиков");
        try{
            sleep(1000*60*3);
        }catch(Exception e){};
        
    }
      
}

class GetUserInfoThread extends Thread{
    String username, id;
    UserSetOld users;
    User user;
    ResultSet rs;
    //HashMap<String, String[]> query;
    
    @Override
    public void run(){
        users = new UserSetOld();
        user = users.getUserInfo(username);
        //user.DBId = id;
        //users.identUserLocation(user, query);
        
        if (!user.isNotExists() && !user.isIsPrivate()){
            //print("Пользователь " + user.username + " не существует");
            DataBase.exeChangeQuery("UPDATE insta_users_ext SET follows='" + user.getFollows() + "', followers='" + user.getFollowed_by() + "'"
                    + ", posts='" + user.getMediaCount() + "', days_from_last_post='" + user.getDaysFromLastPost() + "', bio='" + user.getBiography() + 
                    "', insta_id='" + user.getInstaId() + "' WHERE id = '"+ id +"'");
        }
        
        
        /*user = users.getUserInfo(username);

        if (user.notExists){
            print("Пользователь " + user.username + " не существует");
            //DataBase.exeChangeQuery("DELETE FROM insta_temp_users_unique WHERE id = '"+ id +"'");
        }
        if (user.isPrivate) {
            print("Пользователь " + user.username + " имеет приватный профиль");
            DataBase.exeChangeQuery("DELETE FROM insta_temp_users_unique WHERE id = '"+ id +"'");
        }
        if (!user.notExists && user.mediaCount < 20) {
            print("Пользователь " + user.username + " запостил меньше 20 фоток");
            DataBase.exeChangeQuery("DELETE FROM insta_temp_users_unique WHERE id = '"+ id +"'");
        }    */
    }
}
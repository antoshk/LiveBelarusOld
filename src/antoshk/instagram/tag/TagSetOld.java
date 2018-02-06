/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.tag;
import antoshk.instagram.States;
import antoshk.instagram.user.UserSetOld;
import antoshk.instagram.media.MediaSetOld;
import antoshk.live_belarus.DataBase;
import static antoshk.live_belarus.Utils.print;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author User
 */
public class TagSetOld {
    ArrayList<String> tagNames;
    public int tagCount, newTagCount;
    public void setTags(ArrayList<String> tags){
        tagNames = tags;
    }
    
    
    public void updateTagsCore(){
        //TreeMap <Integer, String> users = new TreeMap();
        States state = new States();
        ResultSet rs, subRs;
        String refTotal, refUsers, lifeTime, popularity, timeToLive, meashureDate, acceleration;
        long time;
        
        //DataBase.exeChangeQuery("TRUNCATE TABLE insta_user_tags");
        
        collectTags(false);

        DataBase.exeChangeQuery("TRUNCATE TABLE insta_tags_save");
        DataBase.exeChangeQuery("INSERT INTO insta_tags_save(id, name, refTotal, refUsers) SELECT id, name, refTotal, refUsers FROM insta_tags");
        
        
        tagCount = Integer.parseInt(DataBase.exeSingleFieldSelectQuery("SELECT COUNT(id) as count FROM insta_tags", "count"));
        print("Всего тэгов: "+tagCount);
        rs = DataBase.exeSelectQuery("SELECT refTotal, refUsers, lifeTime, popularity, timeToLive, meashureDate, acceleration, name"+
                " FROM insta_tags_core");
        try{
            while(rs.next()){
                subRs = DataBase.exeSelectQuery("SELECT refTotal, refUsers FROM insta_tags WHERE name='"+rs.getString("name")+"'");
                time = (new Date()).getTime();
                
                if(subRs.next()){
                    popularity = String.valueOf((rs.getDouble("popularity") + rs.getInt("refUsers") + subRs.getInt("refUsers"))/2);
                    refTotal = String.valueOf(subRs.getInt("refTotal"));
                    refUsers = String.valueOf(subRs.getInt("refUsers"));
                    timeToLive = String.valueOf(60*60*24*3*1000);
                    meashureDate = String.valueOf(time);
                    lifeTime = String.valueOf(time - rs.getLong("meashureDate") + rs.getLong("lifeTime"));
                    acceleration = String.valueOf(subRs.getInt("refUsers") - rs.getInt("refUsers"));
                    
                    DataBase.exeChangeQuery("UPDATE insta_tags_core SET refTotal='"+refTotal+"', refUsers='"+refUsers+"', lifeTime='"+lifeTime+"', popularity='"
                            +popularity+"', timeToLive='"+timeToLive+"', meashureDate='"+meashureDate+"', acceleration='"+acceleration
                            +"' WHERE name ='" + rs.getString("name") + "'");
                    DataBase.exeChangeQuery("DELETE FROM insta_tags WHERE name='"+rs.getString("name")+"'");
                }else{
                    long ttl = rs.getLong("timeToLive") - (time - rs.getLong("meashureDate"));
                    
                    if (ttl > 0){
                        double factor = (time - rs.getLong("meashureDate"))/(1000*60*60*24);
                        popularity = String.valueOf(rs.getDouble("popularity")*(1-0.25*factor));
                        refTotal = "0";
                        refUsers = "0";
                        
                        timeToLive = String.valueOf(ttl);
                        meashureDate = String.valueOf(time);
                        lifeTime = rs.getString("lifeTime");
                        acceleration = String.valueOf(0 - rs.getInt("refUsers"));
                        
                        DataBase.exeChangeQuery("UPDATE insta_tags_core SET lifeTime='"+lifeTime+"', popularity='"
                            +popularity+"', timeToLive='"+timeToLive+"', meashureDate='"+meashureDate+"', acceleration='"+acceleration
                            +"',refTotal='"+refTotal+"', refUsers='"+refUsers+"'  WHERE name ='" + rs.getString("name") + "'");
                    }else{
                        DataBase.exeChangeQuery("DELETE FROM insta_tags_core WHERE name='"+rs.getString("name")+"'");
                    }    

                }
            }
        } catch(Exception e){
            print("Ошибка при прогоне ядра тэгов: " + e.getMessage());
        }        
        
        newTagCount = Integer.parseInt(DataBase.exeSingleFieldSelectQuery("SELECT COUNT(id) as count FROM insta_tags", "count"));
        print("Новых тэгов: "+newTagCount);
        rs = DataBase.exeSelectQuery("SELECT refTotal, refUsers, name FROM insta_tags");
        try{
            while(rs.next()){
                time = (new Date()).getTime();
                popularity = rs.getString("refUsers");
                refTotal = rs.getString("refTotal");
                refUsers = rs.getString("refUsers");
                timeToLive = String.valueOf(60*60*24*3*1000);
                meashureDate = String.valueOf(time);
                lifeTime = "0";
                acceleration = String.valueOf(rs.getInt("refUsers"));
                DataBase.exeChangeQuery("INSERT INTO insta_tags_core SET refTotal='"+refTotal+"', refUsers='"+refUsers+"', lifeTime='"+lifeTime+"', popularity='"
                            +popularity+"', timeToLive='"+timeToLive+"', meashureDate='"+meashureDate+"', acceleration='"+acceleration
                            +"', name ='" + rs.getString("name") + "'");
            }
        } catch(Exception e){
            print("Ошибка при прогоне остатка тэгов: " + e.getMessage());
        }
           
   
    }
    
    
    public void collectTags( boolean fromBegin){
        int startPos;
        States state = new States();
        UserSetOld users;

        
        if(fromBegin){
            DataBase.exeChangeQuery("TRUNCATE TABLE insta_tags");
        }
        
        if ("notFinished".equals(state.getState("tagsCollectState"))){
            startPos = Integer.parseInt(state.getState("startPos"));
            users = UserSetOld.getUserSet(startPos);
        } else {
            DataBase.exeChangeQuery("TRUNCATE TABLE insta_tags");
            state.setState("tagsCollectState", "notFinished");
            state.setState("startPos", "0");
            users = UserSetOld.getUserSet(0);
        }
            
        
        CollectTagThread[] tagThread = new CollectTagThread[5];
        
        boolean thrdNotFound;
        for(int i=0; i<users.getUserCount(); i++){
            thrdNotFound = true;
            while(thrdNotFound){
                for(int k=0; k<5; k++){
                    if(tagThread[k] == null || !tagThread[k].isAlive()){
                        tagThread[k] = new CollectTagThread();
                        tagThread[k].setCounter(users.getDBId(i));
                        tagThread[k].setUsername(users.getUsername(i));
                        tagThread[k].threadN = k;
                        tagThread[k].start();
                        thrdNotFound = false;
                        break;
                    }
                }
            }
        }
        
        state.setState("tagsCollectState", "finished");
        state.setState("startPos", "0");
    }
    
    public void findTagTendentions(){
        ResultSet rs;
        HashSet <String> tags = new HashSet();
        long timeLimit = 1000*60*60*24*4;
        long counter=0;
        Iterator<String> iter;
        
        rs = DataBase.exeSelectQuery("SELECT `id`, `name`, `refTotal`, `refUsers`, `popularity`, `lifeTime`, `acceleration` FROM insta_tags_core_bp ORDER BY refUsers DESC");
        try{
            while(rs.next()){
                if(rs.getLong("lifeTime")<timeLimit){
                    if(counter<10) tags.add(rs.getString("name"));
                    else break;
                    counter++;
                }
            }
        } catch(Exception e){
            print("Ошибка при поиске тенденций по количеству пользователей: " + e.getMessage());
        } 
        
        counter=0;
        rs = DataBase.exeSelectQuery("SELECT `id`, `name`, `refTotal`, `refUsers`, `popularity`, `lifeTime`, `acceleration` FROM insta_tags_core_bp ORDER BY popularity DESC");
        try{
            while(rs.next()){
                if(rs.getLong("lifeTime")<timeLimit){
                    if(counter<10) tags.add(rs.getString("name"));
                    else break;
                    counter++;
                }
            }
        } catch(Exception e){
            print("Ошибка при поиске тенденций по популярности: " + e.getMessage());
        } 
        
        iter = tags.iterator();
        while(iter.hasNext()){
            print(iter.next());
        }
        
        
    }
    
    
    
}

class CollectTagThread extends Thread{
    MediaSetOld medias;
    ArrayList<String> tags;
    long timeAgo;
    String singleQuery, username;
    long threadN, counter;
    
    public void setUsername(String username){
        this.username = username;
    }
    public void setCounter(long cnt){
        this.counter = cnt;
    }
    
    @Override
    public void run(){
        print("Обрабатываю пользователя номер " + counter + ", имя " + username + "в потоке номер " + threadN);

        singleQuery = DataBase.exeSingleFieldSelectQuery("SELECT lastTagCollect FROM insta_users_ext WHERE username = '"+username+"'", "lastTagCollect");
        if (singleQuery != null && !singleQuery.equals("")){
            timeAgo = Long.parseLong(singleQuery);
            timeAgo = ((new Date()).getTime() - timeAgo)/1000 ;
        }else{
            timeAgo = 60*60*24;
        }

        medias = MediaSetOld.getMediaSetByUsername(username, 100, timeAgo);
        DataBase.exeChangeQuery("UPDATE insta_users_ext SET lastTagCollect='"+(new Date()).getTime()+"' WHERE username = '"+username+"'");

        tags = medias.extractTags();
        HashMap<String, Integer> tagMap = new HashMap();
        for(String tag : tags){
            if(tagMap.get(tag) == null){
                tagMap.put(tag, 1);
            }else {
                tagMap.put(tag, tagMap.get(tag)+1);
            }
        }
        
        ResultSet rs;
        States state = new States();
        
        for(Map.Entry<String, Integer> tag : tagMap.entrySet()){
            synchronized(this){
                rs = DataBase.exeSelectQuery("SELECT refTotal, refUsers FROM insta_tags WHERE name ='" + tag.getKey() + "'");
                try{
                    if(rs.next()){
                        DataBase.exeChangeQuery("UPDATE insta_tags SET refTotal='"+(rs.getInt("refTotal") + tag.getValue())+"', refUsers='"
                                +(rs.getInt("refUsers") + 1)+"' WHERE name ='" + tag.getKey() + "'");
                        state.setState("startPos", String.valueOf(counter));
                    } else {
                        DataBase.exeChangeQuery("INSERT INTO insta_tags SET refTotal='"+(tag.getValue())+"', refUsers='1', name ='" + tag.getKey() + "'");
                        state.setState("startPos", String.valueOf(counter));
                    }
                } catch(Exception e){
                    print("Ошибка при добавлении тэга: " + e.getMessage());
                } 
                DataBase.exeChangeQuery("INSERT INTO insta_user_tags SET user_name='"+username+"', tag_name='"+tag.getKey()+"' date = '"+(new Date()).getTime()+"'");
            }
        }


    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.tag;
import antoshk.instagram.dao.*;
import antoshk.instagram.media.MediaList;
import java.util.List;
import java.util.ArrayList;
import antoshk.live_belarus.DataBase;
import java.util.Date;

import antoshk.instagram.dao.DAO;
import antoshk.instagram.entity.*;
import java.util.Iterator;

import antoshk.proxy.ProxyList;
import ru.fuzzysearch.*;
import antoshk.instagram.media.*;
import antoshk.instagram.multiThread.MultiThreader;
import antoshk.live_belarus.SimpleLogger;
import antoshk.live_belarus.Utils;
import static antoshk.live_belarus.Utils.print;

/**
 *
 * @author User
 */
public class TagProc {

    //Получает тэги из поста
    public static List<String> extractTags(MediaList mediaList){
        List<String> tags = new ArrayList();
        List<String> tagBuff;
        
        String[] words;
        String tmpWord=null;
        for(Media media : mediaList){
            if(media.getCaption() != null){
                words = media.getCaption().replaceAll("#", " #").split("[ ,\n:]");
                tagBuff = new ArrayList();
                for(String word : words){
                    if(word.indexOf("#")==0){
                        tmpWord = word.replaceAll("'", "&quot;").replaceAll("[^\\w\\n \\:\\.\\,\\-\\&\\;\\#А-Яа-я]+", "");
                        if(!tmpWord.equals("#")) tagBuff.add(tmpWord); 
                    }
                }
                createTagLinks(tagBuff);
                tags.addAll(tagBuff);
            }
        }

        return tags;
    }
    
    //Создаёт, либо инкрементирует связи между тэгами, которые упоминались в одном посте
    public static void createTagLinks(List<String> tags){
        TagLinkDAO tagLinkDAO = new TagLinkDAO();
        for(int i=0; i<tags.size(); i++){
            for(int k=i+1; k<tags.size(); k++){
                //if (tags.get(i).equals(tags.get(k))) continue;
                synchronized(TagProc.class){
                    TagLink link = tagLinkDAO.getByNames(tags.get(i), tags.get(k));
                    if (link == null) link = tagLinkDAO.getByNames(tags.get(k), tags.get(i));
                    if (link == null){
                        link = new TagLink();
                        link.setName(tags.get(i));
                        link.setLinkedTagName(tags.get(k));
                        link.setLinkCount(1);
                        tagLinkDAO.add(link);
                    } else {
                        link.setLinkCount(link.getLinkCount()+1);
                        tagLinkDAO.update(link);
                    }
                }
            }
        }
    }

    //Обновляет ядро тэгов по данным из временной таблицы, заполненной сборщиком тэгов 
    public static void updateTagsCore(){

        long time;  
        DAO<TempTag> tempTagDAO = new UniDAO(TempTag.class);
        DAO<CoreTag> coreTagDAO = new UniDAO(CoreTag.class);
        
        //DataBase.exeChangeQuery("TRUNCATE TABLE insta_user_tags");
        
        //(new TagCollector()).collectTagsInThreads();
        
        //Копируем таблицу с собранными тэгами во временную таблицу, чтобы можно было, если что, проследить изменения, которые сделала программа
        DataBase.exeChangeQuery("TRUNCATE TABLE insta_tags_save");
        DataBase.exeChangeQuery("INSERT INTO insta_tags_save(id, name, refTotal, refUsers) SELECT id, name, refTotal, refUsers FROM insta_tags");
        
        int portionSize = 100;
        print(Utils.getTimeStamp()+" Всего тэгов: "+tempTagDAO.getCount(), "mainArea");
        
        //Перебор всех тэгов ядра тэгов (загрузка по порциям)
        List<CoreTag> coreTagBuff = coreTagDAO.getFirstNItems(portionSize);
        while(true){
            for (CoreTag cTag: coreTagBuff){
                //Поиск упоминания тэга в таблице с новой информацией
                TempTag tTag = tempTagDAO.getByName(cTag.getName());
                time = (new Date()).getTime();
                //Если новые данные по тэгу есть, обновляем тэг в своответствии с этими данными
                if (tTag != null){
                    cTag.setPopularity((cTag.getPopularity() + cTag.getRefUsers() + tTag.getRefUsers())/2);
                    cTag.setLifeTime(time - cTag.getMeashureDate() + cTag.getLifeTime());
                    cTag.setAcceleration(tTag.getRefUsers() - cTag.getRefUsers());
                    
                    cTag.setTimeToLive(60*60*24*3*1000);
                    cTag.setMeashureDate(time);
                    cTag.setRefTotal(tTag.getRefTotal());
                    cTag.setRefUsers(tTag.getRefUsers());
                    coreTagDAO.update(cTag);
                    //Удаляем данные о тэге из таблицы с новой информацией, чтобы там остались только новые тэги, которых ещё нет в ядре тэгов
                    tempTagDAO.delete(tTag.getId());
                }else{
                    long ttl = cTag.getTimeToLive() - (time - cTag.getMeashureDate());
                    //Если новых данных нет, проверяем, как давно нет новых данных
                    if (ttl > 0){
                        //Если ещё не прошло достаточно много времени, обновляем данные по тэгу, уменьшая значения
                        double factor = (time - cTag.getMeashureDate())/(1000*60*60*24);
                        cTag.setPopularity(cTag.getPopularity()*(1-0.25*factor));
                        cTag.setAcceleration(0 - cTag.getRefUsers());

                        cTag.setTimeToLive(ttl);
                        cTag.setMeashureDate(time);
                        cTag.setRefTotal(0);
                        cTag.setRefUsers(0);
                        coreTagDAO.update(cTag);
                    }else{
                        //Если прошло достаточно много времени, тэг удаляем
                        coreTagDAO.delete(cTag.getId());
                    }                
                }   
            }
            if (coreTagBuff.size() < portionSize) break;
            long lastId = coreTagBuff.get(coreTagBuff.size()-1).getId();
            coreTagBuff = coreTagDAO.getNextNItemsById(lastId, portionSize);
        }
        
        print(Utils.getTimeStamp()+" Новых тэгов: " + tempTagDAO.getCount(), "mainArea");
        
        //Добавляем оставшиеся тэги в ядро тэгов как новые (так же по порциям)
        List<TempTag> tempTagBuff = tempTagDAO.getFirstNItems(portionSize);
        while(true){
            for (TempTag tTag: tempTagBuff){
                time = (new Date()).getTime();
                CoreTag cTag = new CoreTag();
                
                cTag.setPopularity(tTag.getRefUsers());
                cTag.setLifeTime(0);
                cTag.setAcceleration(tTag.getRefUsers());

                cTag.setTimeToLive(60*60*24*3*1000);
                cTag.setMeashureDate(time);
                cTag.setRefTotal(tTag.getRefTotal());
                cTag.setRefUsers(tTag.getRefUsers());
                cTag.setName(tTag.getName());
                coreTagDAO.add(cTag);

            }
            if (tempTagBuff.size() < portionSize) break;
            long lastId = tempTagBuff.get(tempTagBuff.size()-1).getId();
            tempTagBuff = tempTagDAO.getNextNItemsById(lastId, portionSize);
        }        
        print(Utils.getTimeStamp()+" Обновление ядра закончено", "mainArea");
    }

    //Ищет тэги, которые недавно набрали популярность, фильтрует рекламу
    public static void findTagTendentions(){
        List<CoreTag> list;
        int dayDepth = 5;
        int minRefCount = 5;
        CoreTagDAO coreTagDAO = new CoreTagDAO();
        list = coreTagDAO.getNewAndPopularTags(dayDepth, minRefCount);
        MediaSearcher mediaSearcher = new MediaSearcher();
        AdsDetector GADetect = new AdsDetector();
        
        Iterator<CoreTag> iter = list.iterator();
        while(iter.hasNext()){
            CoreTag ct = iter.next();
            //print(ct.getName());
            if(GADetect.checkTag(ct.getName())){
                iter.remove();
                continue;
            }

            MediaList mediaList = mediaSearcher.getMediaListByTag(ct.getName(), dayDepth*60*24*24);
            if (mediaList.size()<10){
                iter.remove();
                continue;                
            }
            GADetect.setMediaList(mediaList);
            if (GADetect.checkCaption()){
                iter.remove();
                continue;    
            }
            /*if (GADetect.checkImages()){
                iter.remove();                
            }*/
            
        }

        for (CoreTag tag: list) print(tag.getName(), "tagArea");
    } 
    
    //возвращает список тэгов, которые чаще, чем два раза встречались в одном посте с данным тэгом
    public static List<String> getLinkedTags(String tagName){
        List<String> result = new ArrayList();
        TagLinkDAO tlDAO = new TagLinkDAO();
        List<TagLink> links = tlDAO.getLinkedTagsByName(tagName);
        for(TagLink link: links){
            if(link.getName().equals(link.getLinkedTagName())) continue;
            if(link.getName().equals(tagName) && link.getLinkCount() > 2) result.add(link.getLinkedTagName());
            if(link.getLinkedTagName().equals(tagName) && link.getLinkCount() > 2) result.add(link.getName());
        }
        return result;
    }
    
    //возвращает список тэгов, которые похожи на данный (отстоят от него на distance перестановок)
    public static List<CoreTag> findSimilarTags(String tag, int distance){
        List<CoreTag> list;
        List<CoreTag> resultList = new ArrayList();
        int portion = 100;
        CoreTagDAO coreTagDAO = new CoreTagDAO();
        list = coreTagDAO.getFirstNReferedTags(portion, 10);
        while(true){
            for (CoreTag cTag: list){
                if(compareTags(tag ,cTag.getName()) <= distance) resultList.add(cTag);
            }
            if (list.size() < portion) break;
            long lastId = list.get(list.size()-1).getId();
            list = coreTagDAO.getNextNReferedTagsById(lastId, portion, 10);
        }
        return resultList;
    }
    
    //Возвращает количество перестановок, которые надо совершить, чтобы получить из одного тэга другой
    public static int compareTags(String tag1, String tag2){
        int result;
        tag1 = tag1.toLowerCase();
        tag2 = tag2.toLowerCase();
        Metric metric = new DamerauLevensteinMetric();
        result = metric.getDistance(tag1, tag2);
        
        
        return result;
    }
    
    public static void runTagCleaner(){
        TagLinkDAO tlDAO = new TagLinkDAO();
    }

    public void collectTagsInThreads(boolean forceFromBegin){
        CollectProcDispatcher dispatcher = new CollectProcDispatcher();
        dispatcher.collectTags(forceFromBegin);
    }
    
        //Собирает тэги со всех пользователей в многопоточном режиме
//    public void collectTagsInThreads(){
//        DAO<State> stateDAO = new UniDAO(State.class);
//        ProxyList.setProxyNeedToBeUsed(true);
//
//        MultiThreader.procCount = ProxyList.getProxyCount() * 2 / 3;
//        MultiThreader mt = new MultiThreader();
//        int portion = 500;
//        DAO<User> userDAO = new UniDAO(User.class);
//        List<User> userList = getFirstNUsersForTagCollect(false, portion);
//
//        while(true){
//            print("Обрабатываю " + portion + " пользователей начиная с " + stateDAO.getByName("startPos").getValue() );
//            mt.run(new TagCollector(), userList);
//            if (userList.size() < portion) break;
//            long lastId = userList.get(userList.size()-1).getId();
//            userList = userDAO.getNextNItemsById(lastId, portion);
//        }
//
//        ProxyList.setProxyNeedToBeUsed(false);
//
//        finishCollect();
//
//    }
    
        //Получает первую порцию пользователей, попутно инициализируя процесс сборки
//    public List<User> getFirstNUsersForTagCollect(boolean forceFromBegin, int count){
//        DAO<State> stateDAO = new UniDAO(State.class);
//        DAO<User> userDAO = new UniDAO(User.class);
//        List<User> userList;
//
//        State state = stateDAO.getByName("tagsCollectState");
//
//        if (state != null && forceFromBegin != true && state.getValue().equals("notFinished")){
//            state = stateDAO.getByName("startPos");
//            userList = userDAO.getNextNItemsById(Integer.parseInt(state.getValue()), count);
//        } else {
//            prepareCollect();
//
//            if (state != null){
//                state.setValue("notFinished");
//                stateDAO.update(state);
//            }else {
//                state = new State();
//                state.setName("tagsCollectState");
//                state.setValue("notFinished");
//                stateDAO.add(state);
//            }
//
//            state = stateDAO.getByName("startPos");
//            if (state != null){
//                state.setValue("0");
//                stateDAO.update(state);
//            } else {
//                state = new State();
//                state.setName("startPos");
//                state.setValue("0");
//                stateDAO.add(state);
//            }
//
//            userList = userDAO.getFirstNItems(count);
//        }
//        return userList;
//    }
    

    
        //Собирает тэги со всех пользоваетелй в однопоточном режиме
//    public void collectTags(){
//        int portion = 500;
//        DAO<User> userDAO = new UniDAO(User.class);
//        List<User> userList = getFirstNUsersForTagCollect(false, portion);
//        while(true){
//            for (User user: userList){
//                print("Обрабатываю пользователя номер " + user.getId() + ", имя " + user.getUsername());
//                //collectUserTags(user.getUsername(), user.getId());
//            }
//            if (userList.size() < portion) break;
//            long lastId = userList.get(userList.size()-1).getId();
//            userList = userDAO.getNextNItemsById(lastId, portion);
//        }
//        finishCollect();
//    }
    
}

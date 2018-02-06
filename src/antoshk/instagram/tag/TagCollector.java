/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.tag;

import antoshk.instagram.dao.*;

import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import antoshk.instagram.dao.DAO;
import antoshk.instagram.dao.TagToUserDAO;
import antoshk.instagram.entity.*;
import antoshk.instagram.media.MediaList;
import antoshk.instagram.media.MediaSearcher;
import antoshk.instagram.multiThread.MultiThreadWrap;
import antoshk.instagram.multiThread.MultiThreadable;
import antoshk.instagram.multiThread.MultiThreader;
import antoshk.live_belarus.Utils;

import static antoshk.live_belarus.Utils.print;

import antoshk.live_belarus.SimpleLogger;

/**
 * @author User
 */
public class TagCollector implements Runnable {

    private String username;
    private DAO<User> userDAO;
    private User user;


    SimpleLogger logger;

    public TagCollector(String username) {
        this.username = username;
        logger = SimpleLogger.getLogger();
        userDAO = new UniDAO(User.class);
        logger.log("Поток " + Thread.currentThread().getName() + ": Получаю объект пользователя " + username + " из БД");
        user = userDAO.getByName(username);

    }

    @Override
    public void run() {
        logger.log("Поток " + Thread.currentThread().getName() + ": Обрабатываю пользователя номер " + user.getId() + ", имя " + username);
        collectUserTags(username, user.getId());
        CollectProcDispatcher.removeFromPool(this);
        logger.log("Поток " + Thread.currentThread().getName() + ": закончил работу");
    }


    //Собирает тэги пользователя во временную таблицу (TempTag)
    public void collectUserTags(String username, long counter) {
        DAO<State> stateDAO = new UniDAO(State.class);

        logger.log("Поток " + Thread.currentThread().getName() + ": Определяю, как давно проверялся пользователь " + username);
        long timeAgo;
        if (user.getLastTagCollect() != null && !user.getLastTagCollect().equals("")) {
            timeAgo = Long.parseLong(user.getLastTagCollect());
            timeAgo = ((new Date()).getTime() - timeAgo) / 1000;
        } else {
            timeAgo = 60 * 60 * 24;
        }

        logger.log("Поток " + Thread.currentThread().getName() + ": Получаю не более 100 медиа пользователя " + username + " с момента последней проверки");
        MediaList mediaList = MediaSearcher.getMediaListByUsername(username, 100, timeAgo);
        logger.log("Поток " + Thread.currentThread().getName() + ": Получил всего " + mediaList.size() + " медиа");

        logger.log("Поток " + Thread.currentThread().getName() + ": Сохраняю в БД дату текущей проверки пользователя " + username);
        user.setLastTagCollect(String.valueOf((new Date()).getTime()));
        userDAO.update(user);

        if (mediaList.size() > 0) {

            logger.log("Поток " + Thread.currentThread().getName() + ": Извлекаю тэги пользователя " + username + " из массива медиа");
            List<String> tags = TagProc.extractTags(mediaList);

            logger.log("Поток " + Thread.currentThread().getName() + ": Подсчитываю количество упоминаний каждого тэга пользователя " + username);
            HashMap<String, Integer> tagMap = new HashMap();
            for (String tag : tags) {
                if (tagMap.get(tag) == null) {
                    tagMap.put(tag, 1);
                } else {
                    tagMap.put(tag, tagMap.get(tag) + 1);
                }
            }

            logger.log("Поток " + Thread.currentThread().getName() + ": Пытаюсь сохранить полученные результаты пользователя " + username + " в базу данных");
            for (Map.Entry<String, Integer> tag : tagMap.entrySet()) {
                addTagToDataBase(tag, username);
            }
        }

        logger.log("Поток " + Thread.currentThread().getName() + ": Обновляю счётчик проверенных пользователей");
        State state = stateDAO.getByName("startPos");
        state.setValue(String.valueOf(counter));
        stateDAO.update(state);
    }

    private synchronized void addTagToDataBase(Map.Entry<String, Integer> tag, String username) {
        logger.log("Поток " + Thread.currentThread().getName() + ": Вхожу в синхронизированный метод сохранения в БД");
        DAO<TempTag> tempTagDAO = new UniDAO(TempTag.class);
        TagToUserDAO tagToUserDAO = new TagToUserDAO();

        logger.log("Поток " + Thread.currentThread().getName() + ": Проверяю, добавляю или обновляю информацию о тэге");
        TempTag tempTag = tempTagDAO.getByName(tag.getKey());
        if (tempTag == null) {
            tempTag = new TempTag();
            tempTag.setName(tag.getKey());
            tempTag.setRefTotal(tag.getValue());
            tempTag.setRefUsers(1);
            tempTagDAO.add(tempTag);
        } else {
            tempTag.setRefTotal(tempTag.getRefTotal() + tag.getValue());
            tempTag.setRefUsers(tempTag.getRefUsers() + 1);
            tempTagDAO.update(tempTag);
        }

        logger.log("Поток " + Thread.currentThread().getName() + ": Проверяю, добавляю или обновляю ссылку на пользователя, который запостил тэг");
        TagToUser tagToUser = tagToUserDAO.getByTagNUserName(username, tag.getKey());
        if (tagToUser != null) {
            tagToUser.setDate(String.valueOf((new Date()).getTime()));
            tagToUserDAO.update(tagToUser);
        } else {
            tagToUser = new TagToUser();
            tagToUser.setTagName(tag.getKey());
            tagToUser.setUserName(username);
            tagToUser.setDate(String.valueOf((new Date()).getTime()));
            tagToUserDAO.add(tagToUser);
        }
        logger.log("Поток " + Thread.currentThread().getName() + ": Выхожу из синхронизированного метода");
    }


}

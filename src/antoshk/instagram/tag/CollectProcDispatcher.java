package antoshk.instagram.tag;

import antoshk.instagram.dao.DAO;
import antoshk.instagram.dao.TagToUserDAO;
import antoshk.instagram.dao.UniDAO;
import antoshk.instagram.entity.State;
import antoshk.instagram.entity.TagLink;
import antoshk.instagram.entity.TempTag;
import antoshk.instagram.entity.User;
import antoshk.live_belarus.SimpleLogger;
import antoshk.live_belarus.Utils;
import antoshk.proxy.ProxyList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static antoshk.live_belarus.Utils.print;


public class CollectProcDispatcher {
    private static BlockingQueue<Runnable> threadPool;
    private static boolean needRestart = false;

    public static void removeFromPool(Runnable proc) {
        boolean result = threadPool.remove(proc);
        if (!result) {
            System.out.println("error");
            System.exit(0);
        }
    }

    public static void proxyCountChanged() {
        needRestart = true;
    }

    public void collectTags(boolean forceFromBegin) {
        TagProc tagProc = new TagProc();
        DAO<State> stateDAO = new UniDAO(State.class);
        ProxyList.setProxyNeedToBeUsed(true);
        int threadCount;
        ExecutorService executor;
        int portion = 100;
        DAO<User> userDAO = new UniDAO(User.class);
        Deque<User> userDeque = new LinkedList<>();
        boolean endReached = false;
        userDeque.addAll(getInitUsers(forceFromBegin, portion));

        while (!endReached && userDeque.size() != 0) {
            threadCount = (ProxyList.getProxyCount() * 2 / 3)+1;
            needRestart = false;
            threadPool = new LinkedBlockingQueue<>(threadCount);
            executor = Executors.newCachedThreadPool();
            while (!needRestart) {
                if (!endReached && userDeque.size() < 10) {
                    List<User> users = userDAO.getNextNItemsById(userDeque.getLast().getId(), portion);
                    if (users.size() > 0) userDeque.addAll(users);
                    else endReached = true;
                }

                User user = userDeque.pollFirst();
                if (user != null) {
                    TagCollector collector = new TagCollector(user.getUsername());
                    try {
                        threadPool.put(collector);
                        executor.submit(collector);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }

            }
            executor.shutdown();
            try {
                print("Жду завершения всех потоков");
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ProxyList.setProxyNeedToBeUsed(false);
        finishCollect();
    }

    private void finishCollect() {
        DAO<State> stateDAO = new UniDAO(State.class);
        State state = stateDAO.getByName("tagsCollectState");
        state.setValue("finished");
        stateDAO.update(state);
        print(Utils.getTimeStamp() + " Сбор тэгов закончен", "mainArea");
    }

    private void prepareNewCollect() {
        DAO<TempTag> ttDAO = new UniDAO(TempTag.class);
        ttDAO.truncate();
        DAO<TagLink> tlDAO = new UniDAO(TagLink.class);
        tlDAO.truncate();
        TagToUserDAO tagToUserDAO = new TagToUserDAO();
        tagToUserDAO.deleteOldRecords();
        SimpleLogger.getLogger().clear();
    }

    //Получает первую порцию пользователей, попутно инициализируя процесс сборки
    private List<User> getInitUsers(boolean forceFromBegin, int count) {

        DAO<State> stateDAO = new UniDAO(State.class);
        DAO<User> userDAO = new UniDAO(User.class);
        List<User> userList;

        State state = stateDAO.getByName("tagsCollectState");

        if (state != null && forceFromBegin != true && state.getValue().equals("notFinished")) {
            state = stateDAO.getByName("startPos");
            userList = userDAO.getNextNItemsById(Integer.parseInt(state.getValue()), count);
        } else {
            prepareNewCollect();
            if (state != null) {
                state.setValue("notFinished");
                stateDAO.update(state);
            } else {
                state = new State();
                state.setName("tagsCollectState");
                state.setValue("notFinished");
                stateDAO.add(state);
            }

            state = stateDAO.getByName("startPos");
            if (state != null) {
                state.setValue("0");
                stateDAO.update(state);
            } else {
                state = new State();
                state.setName("startPos");
                state.setValue("0");
                stateDAO.add(state);
            }

            userList = userDAO.getFirstNItems(count);
        }
        return userList;
    }


}

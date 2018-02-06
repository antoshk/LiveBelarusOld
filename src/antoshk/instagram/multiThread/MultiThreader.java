/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.multiThread;
import java.util.List;
import antoshk.instagram.entity.User;
import antoshk.live_belarus.Utils;
import static antoshk.live_belarus.Utils.print;
import static antoshk.live_belarus.Utils.printWarn;
import java.util.Timer;
import java.util.TimerTask;
/**
 *
 * @author User
 */

//Класс, который запускает потоки
public class MultiThreader {
    public static int procCount = 15;

    public void run(MultiThreadable process, List<User> userList){

        MultiThreadWrap[] tagThread = new MultiThreadWrap[procCount];
        
        boolean thrdNotFound;
        int timeCounter = 0;
        for(User user: userList){
            thrdNotFound = true;
            while(thrdNotFound){
                for(int k=0; k<tagThread.length; k++){
                    if(tagThread[k] == null || !tagThread[k].isAlive()){
                        tagThread[k] = new MultiThreadWrap(process, user.getUsername(), user.getId(), k);
                        tagThread[k].start();
                        thrdNotFound = false;
                        break;
                    }
                }
            }
        }
        
        boolean printed = false;
        timeCounter = 0;
        int stuckThread=10;
        Timer freezeTimer = new Timer();
        FreezeTimerTask freezeTimerTask = new FreezeTimerTask();
        int timeToWait = 1000*60*5;
        
        while(true){
            
            if (!printed){
                printWarn("Жду завершения всех потоков");
                printed = true;
                freezeTimerTask.tagThread = tagThread;
                freezeTimer.schedule(freezeTimerTask, timeToWait);
            }
            
            boolean processFinished = true;
            for(int k=0; k<tagThread.length; k++){
                if(tagThread[k] != null && tagThread[k].isAlive()){
                    processFinished = false;
                    stuckThread = k;
                }
            }
            if (processFinished) {
                freezeTimerTask.cancel();
                freezeTimer.cancel();
                freezeTimer.purge();
                break;
            }
            
        }
    }
}

class FreezeTimerTask extends TimerTask {
    
    MultiThreadWrap[] tagThread;
    
    @Override
    public void run(){
        for(int k=0; k<tagThread.length; k++){
            if(tagThread[k].isAlive()){
                print("Поток " + k + " (пользователь " + tagThread[k].username + " ) жэсточайше завис! Пытаюсь прервать.");
                tagThread[k].interrupt();
            }
        }
    }
    
}
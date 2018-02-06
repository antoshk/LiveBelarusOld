/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;

import antoshk.instagram.multiThread.MultiThreadWrap;
import antoshk.instagram.multiThread.MultiThreadable;

/**
 *
 * @author User
 */
public class TestThreads implements MultiThreadable {
    int threadN;
    MultiThreadWrap thread;
    SimpleLogger logger;
    
    
    public void runProcess(String username, long counter, int threadN, MultiThreadWrap thread){
        this.thread = thread;
        this.threadN = threadN;
        logger = SimpleLogger.getLogger();
        logger.log("Поток " + threadN +  ": Обрабатываю пользователя номер " + counter + ", имя " + username);
        try{
            thread.wait(1000);
        } catch(Exception e){};
        logger.log("Поток " + threadN +  ": обновляю пользователя номер " + counter + ", имя " + username);  
        try{
            thread.wait(1000);
        } catch(Exception e){};
        logger.log("Поток " + threadN +  ": заканчиваю работу с пользователем номер " + counter + ", имя " + username);
    }
    
}

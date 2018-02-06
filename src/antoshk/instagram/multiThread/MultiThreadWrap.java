/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.multiThread;

/**
 *
 * @author User
 */

//Обёртка, которая добавляет классу многопоточный функционал
public class MultiThreadWrap extends Thread {
    protected MultiThreadable process ;
    protected String username;
    protected long counter;
    protected int threadN;
    
    public MultiThreadWrap(MultiThreadable process, String username, long counter, int threadN){
        this.process = process;
        this.username = username;
        this.counter = counter;
        this.threadN = threadN;
    }
    
    @Override
    public void run(){
        process.runProcess(username, counter, threadN, this);
    }
}

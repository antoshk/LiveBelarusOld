/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;

import java.io.File;
import java.io.PrintStream;

/**
 *
 * @author User
 */
public class SimpleLogger {
    
    private String mode;
    private PrintStream stream;
    static private SimpleLogger logger;
    
    private SimpleLogger(){
        clear();
    }
    
    public static SimpleLogger getLogger(){
        if (logger == null) logger = new SimpleLogger();
        return logger;
    }
    
    public void log(String msg){
        stream.println(Utils.getTimeStamp() + " " + msg);
    }
    
    public final void clear(){
        try {
            stream = new PrintStream(new File("log.txt"));
        } catch (Exception e){};    
    }
    
}

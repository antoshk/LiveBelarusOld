/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram;
import antoshk.live_belarus.DataBase;
import static antoshk.live_belarus.Utils.print;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author User
 */
public class States {
    HashMap <String, String> states;
    
    public States(){
        states = new HashMap();
    }
    
    public String getState(String name){
        states = getStates();
        return states.get(name);
    }
    public void setState(String name, String state){
        states = getStates();
        states.put(name, state);
        setStates(states);
    }
    
    HashMap <String, String> getStates(){
        HashMap <String, String> state = new HashMap();
        
        ResultSet rs;
        rs = DataBase.exeSelectQuery("SELECT name, value FROM insta_state");
        try{
            while(rs.next())
                state.put(rs.getString("name"), rs.getString("value"));
        } catch(Exception e){
            print("Ошибка при извлечении переменный состояния: " + e.getMessage());
        }
        
        return state;
    }
    
    void setStates(HashMap <String, String> states){
        DataBase.exeChangeQuery("TRUNCATE TABLE insta_state");
        for(Map.Entry<String,String> element : states.entrySet()){
            DataBase.exeChangeQuery("INSERT INTO insta_state SET name='"+element.getKey()+"', value='"+element.getValue()+"'");
        }
    }
    
    void flushStates(){
        DataBase.exeChangeQuery("TRUNCATE TABLE insta_state");
    }

    
}

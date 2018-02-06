/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;
import static antoshk.live_belarus.Utils.print;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
/**
 *
 * @author User
 */
public class DataBase {
    private static Connection conn;
    private static int opsCount;
    
    public static void init(){
        conn = getDBConnection();
    }
    
    private static Connection getDBConnection(){
        
        String DB_LINK = "jdbc:mysql://localhost:3306/bellive";
        String DB_USER = "livebelarus";
        String DB_PASS = "zeftvgjy";
        
        
        Connection dbConnection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            print("Where is your MySQL JDBC Driver?");
            print(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_LINK, DB_USER, DB_PASS);
        } catch (SQLException e) {
            print("Where is your MySQL JDBC Driver?");
            print(e.getMessage());
        }
        opsCount = 0;
        return dbConnection;
    }
    
    
    public static synchronized int exeChangeQuery(String query){
        //print(query);
        if (conn == null) init();
        try {
            if (opsCount > 1000000) conn.close();
            if (conn.isClosed() || conn == null) init();
            Statement statement = conn.createStatement();
            int rs = statement.executeUpdate(query); 
            opsCount++;
            return rs;
        } catch(Exception e){
            print("Во время выполнения запроса "+ query +" к БД произошла ошибка");
            print(e.getMessage());
            return 0;
        }
        
    }
    public static synchronized ResultSet exeSelectQuery(String query){
        if (conn == null) init();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            return rs;
        } catch(Exception e){
            print("Во время выполнения запроса '"+ query +"' к БД произошла ошибка");
            print(e.getMessage());
            return null;
        }
    }
    
    public static synchronized String exeSingleFieldSelectQuery(String query, String field){
        ResultSet rs = exeSelectQuery(query);
        try {
            if(rs.next()){
                return rs.getString(field);    
            }else
                return null;
        } catch(Exception e){
            print("Во время выполнения единичного запроса '"+ query +"' к БД произошла ошибка");
            print(e.getMessage());
            return null;
        }   
    }
    public static synchronized HashMap<String, String> exeSingleRowSelectQuery(String query){
        ResultSet rs = exeSelectQuery(query);
        try {
            if(rs.next()){
                HashMap<String, String> row = new HashMap(); 
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i=1; i<=columnCount; i++)
                    row.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                return row;    
            }else
                return null;
        } catch(Exception e){
            print("Во время выполнения единичного запроса '"+ query +"' к БД произошла ошибка");
            print(e.getMessage());
            return null;
        }   
    }
    
    public static void readFiles(){
        /*String path = "d:\\Java\\users\\minsk\\";
        File dir = new File(path);
        String[] row;
        ArrayList<String[]> rows = new ArrayList<>();
        HashSet<String> uniqId = new HashSet();    
        
        BufferedReader reader;
        String line;
        int rowCount=0;
        
        for(File file : dir.listFiles()){
            try {
                
                reader = new BufferedReader(new FileReader(file));
                while((line = reader.readLine()) != null){
                    row = line.split(";");
                    if (row[1].equals("Никнейм")) continue;
                    
                    if(uniqId.add(row[1])){
                        //rows.add(row);
                        //rowCount = exeChangeQuery("INSERT INTO insta_users SET insta_id='" + row[0] + "', nickname='" + row[1] + "', rating='" + row[6] + "'");
                    }
                    
                }
                reader.close();
            }catch(Exception e){
                print("При попытке открытия файла возникла ошибка!");
            }   
        }

        //rows.
        print("Всего задейстовано строк БД: " + rowCount);
        print("Всего строк добавлено: " + uniqId.size());
        //print("Всего элементов в первой строке: " + rows.get(0).length);
        //print("Первая ячейка первой строки первого файла выглядит примерно так: " + rows.get(0)[0]);
        //print(uniqId.toString());*/
        String path = "d:\\Java\\BLR\\grodno.txt";
        File file = new File(path);
        BufferedReader reader;
        String line;
        int rowCount=0;
        String[] cols;
        
        try {

            reader = new BufferedReader(new FileReader(file));
            while((line = reader.readLine()) != null){
                cols = line.split(",");
                //if (cols[1].equals("Никнейм")) continue;
                //print("INSERT INTO rev_geo_src SET locality = 'belarus', lat='" + cols[0] + "', lng='" + cols[1] + "'");
                exeChangeQuery("INSERT INTO rev_geo_src SET locality = 'grodno', lat='" + cols[0] + "', lng='" + cols[1] + "'");
            }
            reader.close();
        }catch(Exception e){
            print("При попытке открытия файла возникла ошибка!");
        } 
        
    }
}

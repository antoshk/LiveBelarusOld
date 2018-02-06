/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus.customSearch;

import antoshk.live_belarus.EndPoints;
import antoshk.live_belarus.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author User
 */
public class CustomSearchProc {
    
    //Гуглопоиск по новостям тут.бай при помощи CustomSearchAPI
    public static CustomSearchResult search(String query){
        String searchQuery = EndPoints.CustomSearch + query;
        searchQuery = searchQuery.replaceAll(" ", "+");
        
        String json = Utils.sendRequest(searchQuery);
        CustomSearchResult customSR = new CustomSearchResult();
        customSR.setDate(0);
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(json).getAsJsonObject();
        
        if (root.has("spelling")){
            searchQuery = EndPoints.CustomSearch + root.getAsJsonObject("spelling").get("correctedQuery").getAsString();
            searchQuery = searchQuery.replaceAll(" ", "+");
            json = Utils.sendRequest(searchQuery);
            root = parser.parse(json).getAsJsonObject();
        }
        
        JsonArray data = root.getAsJsonArray("items");
        JsonObject jsonSR;
        if (data != null){
            for (int i=0; i<data.size(); i++){ 
                /*if (data.get(i).getAsJsonObject().getAsJsonObject("pagemap").has("event")){
                    jsonSR = data.get(i).getAsJsonObject().getAsJsonObject("pagemap").getAsJsonArray("event").get(0).getAsJsonObject();
                    String date;
                    if(jsonSR.has("startdate")){
                        date = jsonSR.get("startdate").getAsString();
                    } else if (jsonSR.has("dtstart")){
                        date = jsonSR.get("dtstart").getAsString();
                    } else {
                        continue;
                    }
                    long time = stringToTime(date);
                    if(customSR.getDate() == 0 || customSR.getDate() < time){
                        customSR.setDate(time);
                        customSR.setTitle(jsonSR.get("name").getAsString());
                        customSR.setDescription(jsonSR.get("summary").getAsString());
                        customSR.setLink(jsonSR.get("url").getAsString());
                    }
                    continue;
                }*/

                jsonSR = data.get(i).getAsJsonObject().getAsJsonObject("pagemap").getAsJsonArray("metatags").get(0).getAsJsonObject();
                if (jsonSR.has("article:published_time")){
                    String date = jsonSR.get("article:published_time").getAsString();
                    long time = stringToTime(date);
                    if(customSR.getDate() == 0 || customSR.getDate() < time){
                        customSR.setDate(time);
                        customSR.setTitle(jsonSR.get("og:title").getAsString());
                        customSR.setDescription(jsonSR.get("og:description").getAsString());
                        customSR.setLink(jsonSR.get("og:url").getAsString());
                    }
                }
            }
        } else {
            return null;
        }
        return customSR;
        

        
    }
    public static CustomSearchResult search(List<String> query){
        StringBuffer sr = new StringBuffer("");
        int i=0;
        for(String word: query) {
            if (i==0) sr.append(word);
            else {
                sr.append("+");
                sr.append(word);
            }
            i++;
            if (i>3) break;
        };
        return search(sr.toString());
    }
    
    private static long stringToTime(String date){
        Calendar cal = Calendar.getInstance();
        String year = date.split("T")[0].split("-")[0];            
        String month = date.split("T")[0].split("-")[1];
        String day = date.split("T")[0].split("-")[2];
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        cal.set(Calendar.MONTH, Integer.parseInt(month));
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        return cal.getTime().getTime();
    }
    
}

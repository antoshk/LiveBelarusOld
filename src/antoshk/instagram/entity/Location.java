/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.entity;

import antoshk.instagram.States;

import antoshk.live_belarus.DataBase;
import antoshk.live_belarus.EndPoints;
import antoshk.live_belarus.Utils;
import static antoshk.live_belarus.Utils.print;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 *
 * @author User
 */
//Надо переписать этот класс

final class Sublocation {
    String locName, locId;

    Sublocation(){
    }

    Sublocation(String inpLocName, String inpLocId){
        setLoc(inpLocName,inpLocId);
    }

    void setLoc (String inpLocName, String inpLocId){
        locName = inpLocName;
        locId = inpLocId;
    }
}

public class Location {
    private final String lat, lng;
    private Sublocation[] sublocations;
    static HashMap<String, Double[][]> polygons;
    
    static void loadPolygons(){
        polygons = new HashMap();
        ResultSet rs;
        int rsSize, i=0, k=0;
        String tmp;
        
        rs = DataBase.exeSelectQuery("SELECT locality FROM rev_geo_src GROUP BY locality");
        
        String[] polyNames = new String[0];
        try{
            rs.last();
            rsSize = rs.getRow();
            rs.beforeFirst();
            polyNames = new String[rsSize];
            while(rs.next()){
                polyNames[i] = rs.getString("locality");
                i++;
            }
        }catch(Exception e){
            print("Ошибка при извлечении имён полигонов: " + e.getMessage());
        }
        
        Double[][] points = null;
        tmp="";
        for (i=0, k=0; i<polyNames.length; i++, k=0){
            rs = DataBase.exeSelectQuery("SELECT id, locality, lat, lng FROM rev_geo_src WHERE locality='"+polyNames[i]+"'");

            try{
                rs.last();
                rsSize = rs.getRow();
                rs.beforeFirst();
                points = new Double[2][rsSize];
                while(rs.next()){
                    //tmp = rs.getString("lat");
                    points[0][k] = Double.parseDouble(rs.getString("lng"));
                    points[1][k] = Double.parseDouble(rs.getString("lat"));
                    k++;
                }    
            }catch(Exception e){
                print("Ошибка при извлечении полигонов: " + e.getMessage());
                //print(tmp);
            }
            polygons.put(polyNames[i], points);
        }
    }
    
    public static ArrayList<String> localRevGeocoding(String locId){
        String lat = "", lng = "";
        String url = EndPoints.MediaByLoc;
        url = url.replace("[loc_id]", locId);
        //Utils utils = new Utils();
        String json = Utils.sendRequest(url);
        JsonParser parser = new JsonParser();
        JsonElement elem = parser.parse(json).getAsJsonObject().getAsJsonObject("location").get("lat");
        if(!elem.isJsonNull()){
            lat = parser.parse(json).getAsJsonObject().getAsJsonObject("location").get("lat").getAsString();
            lng = parser.parse(json).getAsJsonObject().getAsJsonObject("location").get("lng").getAsString();    
        }

        return localRevGeocoding(lat, lng);
    }
    
    public static ArrayList<String> localRevGeocoding(String lat, String lng){
        double x, y;
        ArrayList<String> result = new ArrayList(); 
        x = Double.parseDouble(lat);
        y = Double.parseDouble(lng);
        if (polygons == null) loadPolygons();
        for (Map.Entry<String, Double[][]> polygon : polygons.entrySet()){
            if (isDotInPoly(x, y, polygon.getKey())){
                result.add(polygon.getKey());
            }
        }
        return result;
    } 
    
    static boolean isDotInPoly(double lng, double lat, String polyName){
        boolean result=false;
        Double[][] polygon = polygons.get(polyName);
        int vertexCount = polygon[0].length;
        int i;
        double wrkx, yu, yl;//???
        
        for (i=0; i<vertexCount; i++){
            yu = (polygon[1][i] > polygon[1][(i+1)%vertexCount]) ? polygon[1][i] : polygon[1][(i+1)%vertexCount];
            yl = (polygon[1][i] < polygon[1][(i+1)%vertexCount]) ? polygon[1][i] : polygon[1][(i+1)%vertexCount];
            if(polygon[1][(i+1)%vertexCount] - polygon[1][i] > 0){
                wrkx = polygon[0][i] + (polygon[0][(i+1)%vertexCount] - polygon[0][i]) * (lat - polygon[1][i])/(polygon[1][(i+1)%vertexCount] - polygon[1][i]);
            } else {
                wrkx = polygon[0][i];
            }
            if(yu >= lat)
                if(yl < lat){
                    if(lng > wrkx) result = !result;
                    if(Math.abs(lng - wrkx)< 0.00001) return true;
                }
            if((Math.abs(lat - yl) < 0.00001) && (Math.abs(yu-yl) < 0.00001) && (Math.abs(Math.abs(wrkx - polygon[0][i]) + Math.abs(wrkx - polygon[0][(i+1)%vertexCount]) - Math.abs(polygon[0][i] - polygon[0][(i+1)%vertexCount])) < 0.00001)) return true;
            
        }
        return result;

    }
    
    
    public static HashMap revGeocoding(String lat, String lng){
        HashMap <String, String> result = new HashMap();
        String url = EndPoints.RevGeocode;
        url = url.replace("[lat]", lat).replace("[lng]", lng);
        Utils utils = new Utils();
        String json = utils.sendRequest(url);
        
        States state = new States();

        int revGeo = Integer.parseInt(state.getState("revGeo"));
        revGeo++;
        if (revGeo>15000) System.exit(1);
        state.setState("revGeo", revGeo+"");
 
        
        
        JsonParser parser = new JsonParser();
        if("OK".equals(parser.parse(json).getAsJsonObject().get("status").getAsString())){
            JsonArray data = parser.parse(json).getAsJsonObject().getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("address_components");
            for (int i=0; i<data.size(); i++){
                JsonObject component = data.get(i).getAsJsonObject();
                JsonArray types = component.getAsJsonArray("types");
                for (int k=0; k<types.size(); k++){
                    String type = types.get(k).getAsString();
                    if ("country".equals(type)) result.put("country", component.get("long_name").getAsString());
                    if ("locality".equals(type)) result.put("locality", component.get("long_name").getAsString());
                }
            }
            return result;    
        }
        return null;
    }
    
    public static HashMap revGeocoding(String locId){
        String lat = "", lng = "";
        String url = EndPoints.MediaByLoc;
        url = url.replace("[loc_id]", locId);
        Utils utils = new Utils();
        String json = utils.sendRequest(url);
        JsonParser parser = new JsonParser();
        JsonElement elem = parser.parse(json).getAsJsonObject().getAsJsonObject("location").get("lat");
        if(!elem.isJsonNull()){
            lat = parser.parse(json).getAsJsonObject().getAsJsonObject("location").get("lat").getAsString();
            lng = parser.parse(json).getAsJsonObject().getAsJsonObject("location").get("lng").getAsString();    
        }

        return revGeocoding(lat, lng);
    }
    
    
    private Location(String inpLat, String inpLng){
        lat = inpLat;
        lng = inpLng;
    }

    private Sublocation[] findSublocations(){
        String urlString = EndPoints.LocIdByLatLng; 
        urlString = urlString.replace("[lat]", lat).replace("[lng]", lng);
       
        String result;
        Utils utils = new Utils();
        result = utils.sendRequest(urlString);

        return parseLocationJSON(result);
    }
    
    private Sublocation[] parseLocationJSON(String jsonString){
        JsonParser parser = new JsonParser();
        JsonArray data = parser.parse(jsonString).getAsJsonObject().getAsJsonArray("data");
        
        Sublocation subLocs[] = new Sublocation[data.size()];
        String locName, locId;
        
        for (int i=0; i<data.size(); i++){
            JsonObject subLoc = data.get(i).getAsJsonObject();
            locName = subLoc.get("name").getAsString();
            locId = subLoc.get("id").getAsString();
            subLocs[i] = new Sublocation(locName, locId);
        }
        return subLocs;
    }
    
    public static Location getLocationObject (String inpLat, String inpLng){
        Location loc = new Location(inpLat,inpLng);
        loc.sublocations = loc.findSublocations();
        return loc;
    }
    

    
    public int size(){
        return sublocations.length;
    }
    public String getLocId(int i){
        return sublocations[i].locId;
    }
    public String getLocName(int i){
        return sublocations[i].locName;
    }
    public void traceLocs(){
        for(Sublocation sublocs : sublocations){
            print(sublocs.locId + ": " + sublocs.locName);
        }
    }
}

package antoshk.live_belarus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import antoshk.instagram.entity.ProxyWrap;
import antoshk.proxy.ProxyList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Proxy;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author User
 */
public class Utils {

    public static String sendRequest(String urlString) {
        return sendRequest(urlString, ProxyList.isProxyNeedToBeUsed(), null);
    }

    public static String sendRequest(String urlString, boolean useProxy, ProxyWrap proxy) {
        int count = 1;
        String result;
        while (true) {
            print("+");
            try {
                result = executeRequest(urlString, useProxy, null);
                if (count > 1) printWarn("Запрос выполнен с " + count + "попытки");

                return result;
            } catch (SocketException e) {
                count++;
            } catch (IOException e) {
                count++;
            }
        }
    }

    public static String sendSingleProxyRequest(String urlString, ProxyWrap proxy) {
        String result;
        try {
            result = executeRequest(urlString, true, proxy);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private static String executeRequest(String urlString, boolean useProxy, ProxyWrap proxy) throws SocketException, IOException {
        HttpURLConnection conn = null;
        BufferedReader buffReader;
        String line;
        String result = "";

        String tmp = urlString.substring(urlString.indexOf("//") + 2);
        tmp = tmp.substring(0, tmp.indexOf("/"));
        ProxyWrap currentProxy = null;

        print(urlString);
        try {
            URL url = new URL(urlString);

            if (useProxy && proxy == null){
                currentProxy = ProxyList.getRandomProxy();
                if (currentProxy == null) conn = (HttpURLConnection) url.openConnection();
                else conn = (HttpURLConnection) url.openConnection(currentProxy);
            }
            else if (proxy != null) conn = (HttpURLConnection) url.openConnection(proxy);
            else conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Host", tmp);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "ru,en-us;q=0.7,en;q=0.3");
            //conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Keep-Alive", "115");
            conn.setRequestProperty("Connection", "keep-alive");
            //conn.setRequestProperty("Accept-Charset", "windows-1251,utf-8;q=0.7,*;q=0.7");


            buffReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = buffReader.readLine()) != null) {
                result += line;
            }
            buffReader.close();

        } catch (SocketException e) {
            printWarn("Предупреждение: обрыв соединения.");
            if (currentProxy != null) currentProxy.incrementErrorCounter();
            throw e;
        } catch (FileNotFoundException e) {
            printWarn("Предупреждение: пользователь отсутствует. Строка запроса: " + urlString);
            return null;
        } catch (IOException e) {
            printWarn("Предупреждение: доступ запрещён.");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            print(result);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        if (currentProxy != null) currentProxy.flushErrorCounter();
        return result;

    }


    public static String excutePost(String targetURL, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void print(String msg) {
        System.out.println(msg);
        if (Options.appOptions.hasWindowForm()) Options.appOptions.getOutputTextArea().append(msg + "\n");
    }

    public static void print(String msg, String target) {
        if (Options.appOptions.hasWindowForm()) {
            if (target.equals("tagArea")) Options.appOptions.getTagTextArea().append(msg + "\n");
            else if (target.equals("mainArea")) Options.appOptions.getMainTextArea().append(msg + "\n");
        } else {
            System.out.println(msg);
        }
    }

    public static void printWarn(String msg) {
        print((char) 27 + "[31m" + msg + (char) 27 + "[0m");
    }

    public static String getTimeStamp() {
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
        return sdf.format(new Date());
    }

    public static HashMap<String, Boolean> searcher(String srcStr, HashMap<String, String[]> request) {
        HashMap<String, Boolean> result = new HashMap<>();

        for (Map.Entry<String, String[]> searchOption : request.entrySet()) {

            result.put(searchOption.getKey(), false);
            for (String searchStr : searchOption.getValue()) {
                if (srcStr.contains(searchStr) || srcStr.contains(searchStr.toLowerCase()))
                    result.put(searchOption.getKey(), true);
            }
        }
        return result;
    }
}

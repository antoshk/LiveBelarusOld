/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;

import antoshk.instagram.media.MediaSetOld;
import antoshk.instagram.media.*;

import java.sql.*;
import java.io.*;
import java.net.*;

import com.google.gson.*;

import java.awt.*;
//import java.awt.Button;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;

import java.io.File;
import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Timer;
import java.util.TimerTask;

import antoshk.instagram.dao.*;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.List;

import antoshk.instagram.tag.*;

//import static org.bytedeco.javacpp.opencv_imgcodecs.*;
//import org.bytedeco.javacpp.indexer.*;

import org.bytedeco.javacv.*;


//import static org.bytedeco.javacpp.opencv_calib3d.*;

import antoshk.instagram.*;
import antoshk.instagram.compVision.CVProc;

import static antoshk.instagram.compVision.CVProc.bufferedImageToMat;

import antoshk.instagram.entity.*;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import antoshk.instagram.media.AdsDetector;
import antoshk.instagram.multiThread.MultiThreader;

import static antoshk.live_belarus.Utils.print;

import antoshk.live_belarus.customSearch.CustomSearchProc;
import antoshk.live_belarus.customSearch.CustomSearchResult;
import antoshk.proxy.ProxyList;
import antoshk.vk.VKApiProc;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import java.io.ObjectOutputStream;


/**
 * @author User
 */

public class Main {

    static CanvasFrame frame;

    /**
     * @param args the command line arguments
     */


    public static void main(String[] args) throws Exception {


        //CVProc.init();


        // BGProcess window = new BGProcess();
        //  window.setVisible(true);


        long start = (new Date()).getTime();

        (new TagProc()).collectTagsInThreads(false);
        TagProc.updateTagsCore();

        start = ((new Date()).getTime() - start) / (1000*60);


        TagProc.findTagTendentions();
        System.out.println(start);



        //MainProc.makePublication("#сднемматери", true);
        //ProxyList.getRandomProxy();


        //List<CoreTag> list = (new TagProc()).findLinkedTags("#ДеньВоли", 4);
        /*List<CoreTag> list = (new CoreTagDAO()).getNewAndPopularTags(5, 5);
        for (CoreTag tag: list) print(tag.getName());
        print("----------------------------------------------");*/
        //TagProc.findTagTendentions();
        
        
        
        /*MediaList ml = MediaSearcher.getMediaListByTag("#bynetweek", 60*60*24*5);
        List<String> keyWords = MediaProc.getKeyWords(ml);

        
        
        CustomSearchResult csr;
        csr = CustomSearchProc.search(keyWords);
        print(csr.getTitle());
        print(csr.getDescription());
        print(csr.getLink());*/

        //print((new Date()).getTime());


    }

}


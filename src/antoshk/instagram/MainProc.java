/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram;

import antoshk.instagram.media.MediaList;
import antoshk.instagram.media.MediaProc;
import antoshk.instagram.media.MediaSearcher;
import static antoshk.live_belarus.Utils.print;
import antoshk.live_belarus.customSearch.CustomSearchProc;
import antoshk.live_belarus.customSearch.CustomSearchResult;
import antoshk.vk.VKApiProc;
import java.util.List;

/**
 *
 * @author User
 */
public class MainProc {
    public static void makePublication(String tagName, boolean publish){
        MediaList ml = MediaSearcher.getMediaListByTag(tagName, 60*60*24*5);
        
        List<String> keyWords = MediaProc.getKeyWords(ml);
        keyWords.add(0, tagName.replace("#", ""));
        CustomSearchResult csr;
        csr = CustomSearchProc.search(keyWords);
        
        //csr = CustomSearchProc.search("циркдюсолей");
        StringBuffer descr = new StringBuffer();
        descr.append("Запись опубликована, потому что тэг ").append(tagName).append(" был одним из самых популярных вчера.\n");
        
        if (csr != null){
            descr.append("Tut.by по этому поводу сообщает:\n");
            descr.append(csr.getTitle()).append("\n");
            descr.append(csr.getDescription()).append("\n");
            descr.append(csr.getLink()).append("\n");
        }
        if(!publish){
            print(descr.toString());
            print("Всего изображений: " + ml.size());
        }else{
            VKApiProc vk = VKApiProc.getInstance();
            vk.publishMediaList(ml, descr.toString(), tagName);
        }
    }

}

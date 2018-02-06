/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.media;
import java.util.ArrayList;
import java.util.Iterator;
import antoshk.instagram.entity.Media;
import antoshk.instagram.filter.Filter;

/**
 *
 * @author User
 */
//Тот же ArrayList только параметризированный Media и с втроенным фильтром

public class MediaList extends ArrayList<Media> {

    public void filter(Filter comp){
        Iterator<Media> iter = this.iterator();
        while (iter.hasNext()) {
            Media media = iter.next();
            if (comp.filter(media)) iter.remove();
        }
    }
    
}

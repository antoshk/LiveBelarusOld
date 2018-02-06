/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.filter;

import antoshk.instagram.entity.Media;
import java.util.Date;

/**
 *
 * @author User
 */
public class filterMediaByTime implements Filter<Media> {
    long timeAgo;
    
    public filterMediaByTime(long timeAgo){
        this.timeAgo = timeAgo;
    }
    
    @Override
    public boolean filter(Media media){
        long mediaDate;
        mediaDate = Long.parseLong(media.getDate());
        return ((new Date()).getTime() - mediaDate*1000 > timeAgo*1000);
    }    
}

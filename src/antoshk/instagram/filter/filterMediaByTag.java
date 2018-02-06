/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.filter;
import antoshk.instagram.entity.Media;
/**
 *
 * @author User
 */
public class filterMediaByTag implements Filter<Media> {
    String tagName;
    
    public filterMediaByTag(String tagName){
        this.tagName = tagName;
    }
    
    @Override
    public boolean filter(Media media){
        return (media.getCaption() == null || !media.getCaption().toLowerCase().contains(tagName.toLowerCase()));
    }
}

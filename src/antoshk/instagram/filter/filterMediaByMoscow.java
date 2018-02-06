/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.filter;
import antoshk.instagram.filter.Filter;
import antoshk.instagram.entity.Media;
/**
 *
 * @author User
 */
public class filterMediaByMoscow implements Filter<Media> {
    @Override
    public boolean filter(Media media){
        return (media.getCaption() != null && (media.getCaption().contains("oscow") || media.getCaption().contains("осква")));
    }    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;
import antoshk.instagram.entity.CoreTag;
import java.util.List;
import antoshk.live_belarus.HibernateUtil;
import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.query.Query;
/**
 *
 * @author User
 */
public class CoreTagDAO extends UniDAO<CoreTag> {
    
    public CoreTagDAO(){
        super(CoreTag.class);
    }
    
    public List<CoreTag> getNewAndPopularTags(int daysMax, int refMin){
        List<CoreTag> itemList;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction(); 
        itemList = session.createQuery("from CoreTag as ct where ct.lifeTime < "+(daysMax*24*60*60*1000)+" and ct.refUsers >= "+ refMin + "order by ct.refUsers DESC").getResultList();
        session.close();
        return itemList;    
    }
    
    public List<CoreTag> getFirstNReferedTags(int count, int daysMax){
        return getNextNReferedTagsById(0, count, daysMax); 
    }
    
    public List<CoreTag> getNextNReferedTagsById(long from, int count, int daysMax){
        List<CoreTag> itemList;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction(); 
        Query query = session.createQuery("from CoreTag ct where ct.refUsers > 0 and ct.lifeTime < "+(daysMax*24*60*60*1000)+" and ct.id > " + from);
        query.setMaxResults(count);
        itemList = query.getResultList();
        session.close();
        return itemList;        
    }
}

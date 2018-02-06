/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;
import antoshk.instagram.entity.TagToUser;
import antoshk.live_belarus.HibernateUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.Table;
import org.hibernate.query.Query;
/**
 *
 * @author User
 */
public class TagToUserDAO extends UniDAO<TagToUser> {
    public TagToUserDAO(){
        super(TagToUser.class);
    }
    
    public List<TagToUser> getManyByName(String name){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TagToUser> criteria = cb.createQuery(TagToUser.class);
        Root<TagToUser> root = criteria.from(TagToUser.class);
        String colname = "tagName";
        
        criteria.select(root).where(cb.equal(root.get(colname),name));
        List<TagToUser> items = session.createQuery(criteria).getResultList();
        session.close();
        return items;
    }
    
    public TagToUser getByTagNUserName(String userName, String tagName){
        TagToUser ttu;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction(); 
        //Query query = session.createQuery("from TagToUser ttu where tt.tagName = '" + tagName + "' and ttu.userName = '"+userName+"'" );
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TagToUser> criteria = cb.createQuery(TagToUser.class);
        Root<TagToUser> root = criteria.from(TagToUser.class);
        
        criteria.select(root).where(cb.and(cb.equal(root.get("tagName"),tagName), cb.equal(root.get("userName"),userName)));
        
        try{
            ttu = (TagToUser) session.createQuery(criteria).getSingleResult();
        }catch(Exception e){
            ttu = null;
        }
        session.close();
        return ttu; 
    } 
    
    public void deleteOldRecords(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.createQuery("DELETE TagToUser WHERE date < " + String.valueOf((new Date()).getTime() - 1000*60*60*24*7)).executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
    
}

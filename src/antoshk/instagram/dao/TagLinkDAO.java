/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;
import antoshk.instagram.entity.TagLink;
import java.util.List;
import antoshk.live_belarus.HibernateUtil;
import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 *
 * @author User
 */
public class TagLinkDAO extends UniDAO<TagLink> {
    public TagLinkDAO(){
        super(TagLink.class);
    }
    public TagLink getByNames(String tag, String linkedTag){
        TagLink tagLink;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction(); 
        Query query = session.createQuery("from TagLink tl where tl.name = '" + tag + "' and tl.linkedTagName = '"+linkedTag+"'" );
        try{
            tagLink = (TagLink) query.getSingleResult();
        }catch(Exception e){
            tagLink = null;
        }
        session.close();
        return tagLink; 
    }
    
    public List<TagLink> getLinkedTagsByName(String tag){
        List<TagLink> tagLinks;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction(); 
        Query query = session.createQuery("from TagLink tl where tl.name = '" + tag + "' or tl.linkedTagName = '"+tag+"'" );
        tagLinks = query.getResultList();
        session.close();
        return tagLinks; 
    }
    
}

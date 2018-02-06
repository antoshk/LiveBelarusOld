/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;

import antoshk.instagram.entity.User;
import antoshk.live_belarus.HibernateUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;

/**
 *
 * @author User
 */
public class UserDAO extends UniDAO<User> {
    public UserDAO(){
        super(User.class);
    }
    
        public User getByInstaId(long id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        
        criteria.select(root).where(cb.equal(root.get("instaId"),id));
        User item;
        try{
            item = session.createQuery(criteria).getSingleResult();
        } catch (Exception e){
            item = null;
        }
        session.close();
        return item;
    }
}

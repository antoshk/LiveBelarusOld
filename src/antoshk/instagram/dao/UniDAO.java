/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;
import antoshk.live_belarus.HibernateUtil;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.Criteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Expression;
import javax.persistence.Table;
import org.hibernate.query.Query;


/**
 *
 * @author User
 * @param <T>
 */

//Универсальный класс доступа к разным сущностям
public class UniDAO<T extends AbleToSetId> implements DAO<T> {
    private Class<T> uniClass;
    private T somevar;
    
    public UniDAO(Class<T> c){
        uniClass = c;
    }
    
    @Override
    public void clear(){
        
        
        if (uniClass.getSimpleName().equals("User")) {
            System.out.println("Нельзя удалить всех пользователей.");
        } else if(uniClass.getSimpleName().equals("CoreTag")){
            System.out.println("Нельзя очистить ядро тэгов.");
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            session.createQuery("DELETE FROM " + uniClass.getSimpleName()).executeUpdate();
            session.getTransaction().commit();
            session.close();
        }
        
    }
    
    @Override
    public void truncate(){
        if (uniClass.getSimpleName().equals("User")) {
            System.out.println("Нельзя удалить всех пользователей.");
        } else if(uniClass.getSimpleName().equals("CoreTag")){
            System.out.println("Нельзя очистить ядро тэгов.");
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE " + uniClass.getAnnotation(Table.class).name()).executeUpdate();
            session.getTransaction().commit();
            session.close();
        }    
    }
    
    @Override
    public void add(T item){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(item);
        session.getTransaction().commit();
        session.close();
    }
    
    
    @Override
    public void delete(long id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        try{
            T item = uniClass.newInstance();
            item.setId(id);
            session.delete(item);
        }catch(Exception e){}
        session.getTransaction().commit();
        session.close();
    }
    
    @Override
    public void update(T item){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.update(item);
        session.getTransaction().commit();
        session.close();
    }
    
    @Override
    public T getById(long id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        T item = (T) session.get(uniClass, id);
        session.close();
        return item;    
    }
    
    @Override
    public T getByName(String name){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(uniClass);
        Root<T> root = criteria.from(uniClass);
        String colname;
        if (uniClass.getSimpleName().equals("User")) colname = "username";
        else colname = "name";
        
        criteria.select(root).where(cb.equal(root.get(colname),name));
        T item;
        try{
            item = session.createQuery(criteria).getSingleResult();
        } catch (Exception e){
            item = null;
        }
        session.close();
        return item;
    }
    
    @Override
    public List<T> getAll(){
        List<T> itemList = new ArrayList();
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(uniClass);
        Root<T> root = criteria.from(uniClass);
        criteria.select(root);
        
        itemList = session.createQuery(criteria).getResultList();
        session.close();
        
        return itemList;
    }
    
    @Override
    public List<T> getByIdFrom(long fromId){
        List<T> itemList;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(uniClass);
        Root<T> root = criteria.from(uniClass);
        Expression<Integer> id = root.get("id");
        criteria.select(root).where(cb.gt(id, fromId));
        itemList = session.createQuery(criteria).getResultList();
        session.close();
        return itemList;    
    }  
    
    @Override
    public long getCount(){
        long count;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction(); 
        count = (long) session.createQuery("select count(*) from "+uniClass.getSimpleName()).uniqueResult();
        session.close();
        return count;
    }
    
    public List<T> getFirstNItems(int count){
        List<T> itemList;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(uniClass);
        Root<T> root = criteria.from(uniClass);
        criteria.select(root);
        Query query = session.createQuery(criteria);
        query.setMaxResults(count);
        itemList = query.getResultList();
        session.close();
        return itemList;        
    }
    public List<T> getNextNItemsById(long from, int count){
        List<T> itemList;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(uniClass);
        Root<T> root = criteria.from(uniClass);
        Expression<Integer> id = root.get("id");
        criteria.select(root).where(cb.gt(id, from));
        Query query = session.createQuery(criteria);
        query.setMaxResults(count);
        itemList = query.getResultList();
        session.close();
        return itemList;        
    }
    
    
}

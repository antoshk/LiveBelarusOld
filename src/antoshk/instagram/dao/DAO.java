/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;
import java.util.List;

/**
 *
 * @author User
 */
public interface DAO<T> {
    public void clear();
    public void truncate();
    public void add(T item);
    public void delete(long id);
    public void update(T item);
    public T getById(long id);
    public T getByName(String name);
    public List<T> getAll();
    public List<T> getByIdFrom(long fromId); 
    public long getCount();
    public List<T> getFirstNItems(int count);
    public List<T> getNextNItemsById(long from, int count);
}

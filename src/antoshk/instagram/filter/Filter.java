/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.filter;

/**
 *
 * @author User
 */
public interface Filter<T> {
    public boolean filter(T item);
}

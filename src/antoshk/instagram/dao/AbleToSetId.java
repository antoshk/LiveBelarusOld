/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.dao;

/**
 *
 * @author User
 */

//Класс, от которого наследуются все классы, которые могут выступать в качестве дженерика для класса универсального ДАО
public abstract class AbleToSetId {
    abstract public void setId(long id);
}

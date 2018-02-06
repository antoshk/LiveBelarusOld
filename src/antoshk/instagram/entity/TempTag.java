/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.entity;

import antoshk.instagram.dao.AbleToSetId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Id;

/**
 *
 * @author User
 */
@Entity
@Table(name = "insta_tags")

public class TempTag extends AbleToSetId{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private int refTotal;
    private int refUsers;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the refTotal
     */
    public int getRefTotal() {
        return refTotal;
    }

    /**
     * @param refTotal the refTotal to set
     */
    public void setRefTotal(int refTotal) {
        this.refTotal = refTotal;
    }

    /**
     * @return the refUsers
     */
    public int getRefUsers() {
        return refUsers;
    }

    /**
     * @param refUsers the refUsers to set
     */
    public void setRefUsers(int refUsers) {
        this.refUsers = refUsers;
    }
}

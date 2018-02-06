/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.entity;

import antoshk.instagram.dao.AbleToSetId;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 *
 * @author User
 */
@Entity
@Table(name = "insta_tags_core")

public class CoreTag extends AbleToSetId{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private int refTotal;
    private int refUsers;
    private long timeToLive;
    private double popularity;
    private long lifeTime;
    private int acceleration;
    private long meashureDate;

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

    /**
     * @return the timeToLive
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     * @param timeToLive the timeToLive to set
     */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * @return the popularity
     */
    public double getPopularity() {
        return popularity;
    }

    /**
     * @param popularity the popularity to set
     */
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    /**
     * @return the lifeTime
     */
    public long getLifeTime() {
        return lifeTime;
    }

    /**
     * @param lifeTime the lifeTime to set
     */
    public void setLifeTime(long lifeTime) {
        this.lifeTime = lifeTime;
    }

    /**
     * @return the acceleration
     */
    public int getAcceleration() {
        return acceleration;
    }

    /**
     * @param acceleration the acceleration to set
     */
    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * @return the meashureDate
     */
    public long getMeashureDate() {
        return meashureDate;
    }

    /**
     * @param meashureDate the meashureDate to set
     */
    public void setMeashureDate(long meashureDate) {
        this.meashureDate = meashureDate;
    }
}

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
import javax.persistence.Transient;

/**
 *
 * @author User
 */
@Entity
@Table(name = "insta_users_ext")

public class User extends AbleToSetId{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private long id;
    
    @Column (name = "username")
    private String username;
    
    @Column (name = "insta_id")
    private String instaId;
    
    @Column (name = "bio")
    private String biography;
    
    @Column (name = "lastTagCollect")
    private String lastTagCollect;
    
    @Column (name = "follows")
    private int follows;
    
    @Column (name = "followers")
    private int followed_by;
    
    @Column (name = "posts")
    private int mediaCount;
    
    @Column (name = "days_from_last_post")
    private int daysFromLastPost;
    
    @Transient
    private boolean isPrivate;
    @Transient
    private boolean notExists;
    @Transient
    private boolean fromMinsk;
    @Transient
    private boolean fromBelarus;

    public void copy(User user){
        username = user.getUsername();
        instaId = user.getInstaId();
        biography = user.getBiography();
        id = user.getId();
        lastTagCollect = user.getLastTagCollect();
        follows = user.getFollows();
        followed_by = user.getFollowed_by();
        mediaCount = user.getMediaCount();
        daysFromLastPost = user.getDaysFromLastPost();
    }
    
    

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the instaId
     */
    public String getInstaId() {
        return instaId;
    }

    /**
     * @param userId the instaId to set
     */
    public void setInstaId(String userId) {
        this.instaId = userId;
    }

    /**
     * @return the biography
     */
    public String getBiography() {
        return biography;
    }

    /**
     * @param biography the biography to set
     */
    public void setBiography(String biography) {
        this.biography = biography;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param DBId the id to set
     */
    public void setId(long DBId) {
        this.id = DBId;
    }

    /**
     * @return the follows
     */
    public int getFollows() {
        return follows;
    }

    /**
     * @param follows the follows to set
     */
    public void setFollows(int follows) {
        this.follows = follows;
    }

    /**
     * @return the followed_by
     */
    public int getFollowed_by() {
        return followed_by;
    }

    /**
     * @param followed_by the followed_by to set
     */
    public void setFollowed_by(int followed_by) {
        this.followed_by = followed_by;
    }

    /**
     * @return the mediaCount
     */
    public int getMediaCount() {
        return mediaCount;
    }

    /**
     * @param mediaCount the mediaCount to set
     */
    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    /**
     * @return the daysFromLastPost
     */
    public int getDaysFromLastPost() {
        return daysFromLastPost;
    }

    /**
     * @param daysFromLastPost the daysFromLastPost to set
     */
    public void setDaysFromLastPost(int daysFromLastPost) {
        this.daysFromLastPost = daysFromLastPost;
    }

    /**
     * @return the isPrivate
     */
    public boolean isIsPrivate() {
        return isPrivate;
    }

    /**
     * @param isPrivate the isPrivate to set
     */
    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    /**
     * @return the notExists
     */
    public boolean isNotExists() {
        return notExists;
    }

    /**
     * @param notExists the notExists to set
     */
    public void setNotExists(boolean notExists) {
        this.notExists = notExists;
    }

    /**
     * @return the fromMinsk
     */
    public boolean isFromMinsk() {
        return fromMinsk;
    }

    /**
     * @param fromMinsk the fromMinsk to set
     */
    public void setFromMinsk(boolean fromMinsk) {
        this.fromMinsk = fromMinsk;
    }

    /**
     * @return the fromBelarus
     */
    public boolean isFromBelarus() {
        return fromBelarus;
    }

    /**
     * @param fromBelarus the fromBelarus to set
     */
    public void setFromBelarus(boolean fromBelarus) {
        this.fromBelarus = fromBelarus;
    }

    /**
     * @return the lastTagCollect
     */
    public String getLastTagCollect() {
        return lastTagCollect;
    }

    /**
     * @param lastTagCollect the lastTagCollect to set
     */
    public void setLastTagCollect(String lastTagCollect) {
        this.lastTagCollect = lastTagCollect;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.entity;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author User
 */
public class Media {
    private String id;
    private String code;
    private String caption;
    private int commentsCount;
    private int likesCount;
    private String link;
    private String date; 
    private String locId;
    private String ownerId;
    private boolean applyable;
    private BufferedImage cascadedImage;
    private boolean isFromMinsk;
    private File file;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param Id the id to set
     */
    public void setId(String Id) {
        this.id = Id;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the commentsCount
     */
    public int getCommentsCount() {
        return commentsCount;
    }

    /**
     * @param commentsCount the commentsCount to set
     */
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    /**
     * @return the likesCount
     */
    public int getLikesCount() {
        return likesCount;
    }

    /**
     * @param likesCount the likesCount to set
     */
    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param Link the link to set
     */
    public void setLink(String Link) {
        this.link = Link;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param Date the date to set
     */
    public void setDate(String Date) {
        this.date = Date;
    }

    /**
     * @return the locId
     */
    public String getLocId() {
        return locId;
    }

    /**
     * @param locId the locId to set
     */
    public void setLocId(String locId) {
        this.locId = locId;
    }

    /**
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return the applyable
     */
    public boolean isApplyable() {
        return applyable;
    }

    /**
     * @param applyable the applyable to set
     */
    public void setApplyable(boolean applyable) {
        this.applyable = applyable;
    }

    /**
     * @return the cascadedImage
     */
    public BufferedImage getCascadedImage() {
        return cascadedImage;
    }

    /**
     * @param cascadedImage the cascadedImage to set
     */
    public void setCascadedImage(BufferedImage cascadedImage) {
        this.cascadedImage = cascadedImage;
    }

    /**
     * @return the isFromMinsk
     */
    public boolean isIsFromMinsk() {
        return isFromMinsk;
    }

    /**
     * @param isFromMinsk the isFromMinsk to set
     */
    public void setIsFromMinsk(boolean isFromMinsk) {
        this.isFromMinsk = isFromMinsk;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }
}

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
@Table(name = "insta_tag_links")
public class TagLink extends AbleToSetId{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String linkedTagName;
    private int linkCount;

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
     * @return the linkedTagName
     */
    public String getLinkedTagName() {
        return linkedTagName;
    }

    /**
     * @param linkedTagName the linkedTagName to set
     */
    public void setLinkedTagName(String linkedTagName) {
        this.linkedTagName = linkedTagName;
    }

    /**
     * @return the linkCount
     */
    public int getLinkCount() {
        return linkCount;
    }

    /**
     * @param linkCount the linkCount to set
     */
    public void setLinkCount(int linkCount) {
        this.linkCount = linkCount;
    }

    
}

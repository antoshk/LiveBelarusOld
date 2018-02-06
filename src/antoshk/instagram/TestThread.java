/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram;

import antoshk.instagram.dao.TagToUserDAO;
import java.util.List;
import antoshk.instagram.entity.TagToUser;

/**
 *
 * @author User
 */
public class TestThread extends Thread {
    @Override
    public void run(){
        double rnd = Math.random();
        System.out.println("Process "+ rnd+ " starts and try to connect DB");
         TagToUserDAO ttuDAO = new TagToUserDAO();
         TagToUser ttuList = ttuDAO.getById(1);
         System.out.println("Process "+ rnd+ " refused connection");

    }
}

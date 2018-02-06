/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.user;

import antoshk.instagram.dao.DAO;
import antoshk.instagram.dao.UniDAO;
import antoshk.instagram.entity.State;
import antoshk.instagram.entity.User;

import java.util.List;

/**
 *
 * @author User
 */
public class UserProc {

    //Получает первую порцию пользователей, попутно инициализируя процесс сборки
//    public List<User> getUsers(int count){
//        DAO<State> stateDAO = new UniDAO(State.class);
//        DAO<User> userDAO = new UniDAO(User.class);
//        List<User> userList;
//
//        State state = stateDAO.getByName("tagsCollectState");
//
//        if (state != null && forceFromBegin != true && state.getValue().equals("notFinished")){
//            state = stateDAO.getByName("startPos");
//            userList = userDAO.getNextNItemsById(Integer.parseInt(state.getValue()), count);
//        } else {
//
//            if (state != null){
//                state.setValue("notFinished");
//                stateDAO.update(state);
//            }else {
//                state = new State();
//                state.setName("tagsCollectState");
//                state.setValue("notFinished");
//                stateDAO.add(state);
//            }
//
//            state = stateDAO.getByName("startPos");
//            if (state != null){
//                state.setValue("0");
//                stateDAO.update(state);
//            } else {
//                state = new State();
//                state.setName("startPos");
//                state.setValue("0");
//                stateDAO.add(state);
//            }
//
//            userList = userDAO.getFirstNItems(count);
//        }
//        return userList;
//    }
//    public List<User> getUsers(int offset, int count){
//
//    }
}

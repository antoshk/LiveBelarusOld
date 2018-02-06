/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.multiThread;

/**
 *
 * @author User
 */

//Интерфейс для запуска в многопоточном режиме методов разных классов
public interface MultiThreadable {
    public void runProcess(String username, long counter, int threadN, MultiThreadWrap thread);
}

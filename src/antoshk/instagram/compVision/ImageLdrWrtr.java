/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.instagram.compVision;

import static antoshk.live_belarus.Utils.print;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 *
 * @author User
 */
public class ImageLdrWrtr {
    
    //Загружает изображение в память
    public static BufferedImage loadImage(String path) {
        URL url;
        File file;
        BufferedImage bufImage;
        
        try {
            if(path.startsWith("http")){
                url = new URL(path);
                bufImage = ImageIO.read(url);
            } else {
                file = new File(path);
                bufImage = ImageIO.read(file);
            }
            
        } catch (Exception e) {
            print("Что-то не то с адресом картинки");
            print(e.getMessage());
            return null;
        }
        return bufImage;
    }
    
    //записывает изображение в файл
    public static File writeImage(File file, BufferedImage bufImage) {  
        try {
            ImageIO.write(bufImage, "jpg", file);            
        } catch (Exception e) {
            print("Что-то не то с адресом картинки");
            print(e.getMessage());
            return null;
        }
        return file;
    }    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpk.tower.defense;

import java.awt.Point;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pelzinga010800
 */
public class Player implements Serializable{
    double x,y;
    double health, maxHealth, speed;
    double money, kills;
    int speedPerk, regenPerk, healthPerk;
    Gun gun1, gun2, currGun, mysteryGun;
    double dx, dy;
    double angle;
    boolean onEntity[];
    boolean nearPassage[];
    long timeToRegen;
    long creationTime;
    Player(Gun gun){
        x = 320;
        y = 240;
        this.currGun = gun;
        this.gun1 = gun;
        maxHealth = 100;
        health = 100;
        money = 0;
        speed = 1;
        speedPerk = 0;
        regenPerk = 0;
        healthPerk = 0;
        onEntity = new boolean[6];
        nearPassage = new boolean[6];
        creationTime = System.currentTimeMillis();
        
        for (int i = 0; i < onEntity.length; i++) {
            onEntity[i] = false;
            nearPassage[i] = false;
        }
        //onEntity = false;
    }
    public void move(){
        x += dx;
        y += dy;
        
        x = Math.round(x * 10);
        x /= 10;
        
        y = Math.round(y * 10);
        y /= 10;
    }
    public void calcSpeed(char calcKeys[], Point mouseCords){
        double sideX, sideY, hypotenuse;
        double mouseX, mouseY;
        char type = calcKeys[0];
        
        mouseX = (int)(mouseCords.x + x - 320);
        mouseY = (int)(mouseCords.y + y - 240);
        
        sideX = x - mouseX;
        sideY = y - mouseY;
        hypotenuse = Math.sqrt((sideX*sideX) + (sideY * sideY));
        
        
        if((calcKeys[0] == 'd' && calcKeys[1] == 's') || (calcKeys[0] == 'a' && calcKeys[1] == 'w')){ //Quadrant 4 and Quadrant 2
            angle = Math.acos(sideX/hypotenuse) + Math.PI/4;
            
            if(angle >= Math.PI/4 && angle <= Math.PI && sideY < 0){ //Below player
                
            }else if(angle > Math.PI && sideY < 0){ //Below player
                angle = (Math.PI * 2) - angle;
            }else if(angle >= Math.PI/4 && angle <= Math.PI/2 && sideY > 0){ //Above player
                angle = (Math.PI/2) - angle;
            }else if(angle > Math.PI/2 && sideY > 0){ //Above player
                angle = angle - Math.PI/2;
            }
            
            if(calcKeys[0] == 'd'){
                if(sideX < 0){
                    sideX *= -1;
                }
                if(sideY < 0){
                    sideY *= -1;
                }
                dx = (((angle/Math.PI)/2) + 0.5) * 1.2 * (sideX/hypotenuse) * speed;
                dy = (((angle/Math.PI)/2) + 0.5) * 1.2 * (sideY/hypotenuse) * speed;
            }else if(calcKeys[0] == 'a'){
                if(sideX > 0){
                    sideX *= -1;
                }
                if(sideY > 0){
                    sideY *= -1;
                }
                dx = ((((Math.PI - angle)/Math.PI)/2) + 0.5) * 1.2 * (sideX/hypotenuse) * speed;
                dy = ((((Math.PI - angle)/Math.PI)/2) + 0.5) * 1.2 * (sideY/hypotenuse) * speed;
            }
            if(dx < speed/2 && dx >= 0){
                dx = speed/2;
            }else if(dx > -speed/2 && dx <= 0){
                dx = -speed/2;
            }else if(dy <= speed/2 && dy > 0){
                dy = speed/2;
            }else if(dy > -speed/2 && dy <= 0){
                dy = -speed/2;
            }
        }else if((calcKeys[0] == 'd' && calcKeys[1] == 'w') || (calcKeys[0] == 'a' && calcKeys[1] == 's')){ //Quadrant 1 and Quadrant 3
            angle = Math.acos(sideY/hypotenuse) + Math.PI/4;
            
            if(angle >= Math.PI/4 && angle <= Math.PI && sideX > 0){
            }else if(angle > Math.PI && sideX > 0){
                angle = (Math.PI * 2 - angle);
            }else if(angle <= Math.PI/2 && angle >= Math.PI/4 && sideX < 0){
                angle = (Math.PI/2) - angle;
            }else if(angle > Math.PI/2 && sideX < 0){
                angle = angle - Math.PI/2;
            }
            
            if(calcKeys[1] == 's'){
                if(sideX < 0){
                    sideX *= -1;
                }
                if(sideY < 0){
                    sideY *= -1;
                }
                dx = (((angle/Math.PI)/2) + 0.5) * 1.2 * -(sideX/hypotenuse) * speed;
                dy = (((angle/Math.PI)/2) + 0.5) * 1.2 * (sideY/hypotenuse) * speed;
            }else if(calcKeys[1] == 'w'){
                if(sideX > 0){
                    sideX *= -1;
                }
                if(sideY > 0){
                    sideY *= -1;
                }
                dx = ((((Math.PI - angle)/Math.PI)/2) + 0.5) * 1.2 * -(sideX/hypotenuse) * speed;
                dy = ((((Math.PI - angle)/Math.PI)/2) + 0.5) * 1.2 * (sideY/hypotenuse) * speed;
            }
            if(dx < speed/2 && dx >= 0){
                dx = speed/2;
            }else if(dx > -speed/2 && dx <= 0){
                dx = -speed/2;
            }else if(dy <= speed/2 && dy > 0){
                dy = speed/2;
            }else if(dy > -speed/2 && dy <= 0){
                dy = -speed/2;
            }
        }else if(calcKeys[0] == 'a' || calcKeys[0] == 'd'){
            angle = Math.acos(sideX/hypotenuse);
            if(calcKeys[0] == 'a'){
                dx = -((((Math.PI - angle)/Math.PI)/2) + 0.5) * 1.2 * speed; //one unit * 2
            }else if(calcKeys[0] == 'd'){
                dx = (((angle/Math.PI)/2) + 0.5) * 1.2 * speed; // one unit * 2
            }
        }else if(calcKeys[1] == 'w' || calcKeys[1] == 's'){
            angle = Math.acos(sideY/hypotenuse);
            if(calcKeys[1] == 'w'){
                dy = -((((Math.PI - angle)/Math.PI/2) + 0.5) * 1.2 * speed);
            }else if(calcKeys[1] == 's'){
                dy = (((angle/Math.PI)/2) + 0.5) * 1.2 * speed;
            }
        }
        
        double finNum = Math.sqrt((dy*dy) + (dx*dx));
        //System.out.println(finNum);
    }
    public double calcAngle(Point mouseCords){
        double sideX, sideY, hypotenuse;
        double mouseX, mouseY;
        double angle;
        
        mouseX = (int)(mouseCords.x + x - 320);
        mouseY = (int)(mouseCords.y + y - 240);
        
        sideX = x - mouseX;
        sideY = y - mouseY;
        
        hypotenuse = Math.sqrt((sideX*sideX) + (sideY * sideY));
        
        angle = Math.acos(sideX/hypotenuse);
        if(mouseY > y){
            angle = Math.PI + (Math.PI - angle);
        }
        return angle;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public void setY(double y){
        this.y = (int) y;
    }
    public void setX(double x){
        this.x = (int) x;
    }
    public double getDy(){
        return dy;
    }
    public double getDx(){
        return dx;
    }
    public void setDx(double dx){
        this.dx = dx;
    }
    public void setDy(double dy){
        this.dy = dy;
    }
    @Override
    public String toString() {
        String objString = "";
//        objString += x + " ";
//        objString += y + " ";
//        objString += health + " ";
//        objString += maxHealth + " ";
//        objString += money + " ";
//        objString += kills + " ";
//        objString += speedPerk + " ";
//        objString += regenPerk  + " ";
//        objString += healthPerk + " ";
//        
//        objString += gun1.toString();
//        if(gun2 != null){
//            objString += gun2.toString();
//        }
        for (int i = 0; i < 10; i++) {
            
        }
        
        for (Field f : getClass().getDeclaredFields()) {
            String name = f.getName();
            if("currGun".equals(name)){
                break;
            }
            Class type = f.getType();
            try {
                if("gun1".equals(name)){
                    Gun value = (Gun)f.get(this);
                    objString += value;
                }else if(gun2 != null && "gun2".equals(name)){
                    Gun value = (Gun)f.get(this);
                    objString += value;
                }else if(type == int.class){
                    int value = (int) f.get(this);
                    objString += value + " ";
                }else if(type == double.class){
                    Double value = (Double) f.get(this);
                    objString += value + " ";
                }
                //field.getType()
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(objString);
        return objString;
    }
}
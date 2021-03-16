/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpk.tower.defense;

/**
 *
 * @author pelzinga010800
 */
public class Bullet {
    int x, y;
    int x1, y1, x2, y2;
    long initTime, finalTime;
    float transparency;
    
    Bullet(int x, int y, long initTime){
        this.initTime = initTime;
        finalTime = initTime + 100; //+ some constant c
        this.x = x;
        this.y = y;
        transparency = 1;
    }
    public void fade(){
        
        //transparency = (finalTime - System.currentTimeMillis())/250 ;
        //System.out.println((finalTime - System.currentTimeMillis()));
        //System.out.println(finalTime);
        //System.out.println(System.nanoTime());
        //transparency -= 0.01;
    }
}

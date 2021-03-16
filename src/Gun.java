/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpk.tower.defense;

/**
 *
 * @author Patrick
 */
public class Gun {
    double damage, piercing;
    int clipSize, currClip; 
    int reloadTime; //reloadTime in milliseconds
    int fireRate; //in milliseconds, time between each bullet firing
    char type; //semi auto, or full auto, S or F
    long reloadFinish;
    boolean reloading;
    int ID;
    static int numOfGuns = 0;
    
    Gun(double damage, int clipSize, int reloadTime, int fireRate, double piercing){
        this.damage = damage;
        this.clipSize = clipSize;
        currClip = clipSize;
        this.piercing = piercing;
        this.reloadTime = reloadTime;
        this.fireRate = fireRate;
        ID = numOfGuns;
        numOfGuns++;
    }
    
    public String toString(){
        String objString = "";
        
        objString += damage + " ";
        objString += clipSize + " ";
        objString += reloadTime  + " ";
        objString += fireRate + " ";
        objString += piercing + " ";
        //objString += currClip + " ";
        
        return objString;
    }
}

//need to implement firerate, reload time
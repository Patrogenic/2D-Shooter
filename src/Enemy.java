/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpk.tower.defense;

import java.util.Random;

/**
 *
 * @author Patrick
 */
public class Enemy {
    double x, y, dx, dy, health, speed, damage;
    double atckCoolDown, stunCoolDown;
    long timeToAttack; //Time in System milliseconds to determine when to attack next
    long timeToMove; //Time in System millisevonds to determine when to move after being frozen
    int r, b, g;
    Random rand;
    
    Enemy(int x, int y, double health, double speed, double damage){
        this.x = x;
        this.y = y;
        this.health = health;
        this.speed = speed;
        this.damage = damage;
        atckCoolDown = 750; //In milliseconds
        stunCoolDown = 50; //In milliseconds
        rand = new Random();
        r = (int)(rand.nextFloat() * 256);
        b = (int)(rand.nextFloat() * 256);
        g = (int)(rand.nextFloat() * 256);
    }
    public void move(Player player){
        double sideX, sideY, hypotenuse;
        
        sideX = player.x - x;
        sideY = player.y - y;
        
        hypotenuse = Math.sqrt(Math.pow(sideX, 2) + Math.pow(sideY, 2)); //Pythagorean theorem applied
        
        dx = sideX/hypotenuse;
        dy = sideY/hypotenuse;
        
        x += dx*speed;
        y += dy*speed;
    }
    public String toString(){
        String objString = "";
        
        objString += x + " ";
        objString += y + " ";
        objString += health + " ";
        objString += speed + " ";
        objString += damage + " ";
        objString += r + " ";
        objString += b + " ";
        objString += g + " ";
        
        return objString;
    }
}

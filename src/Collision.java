/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpk.tower.defense;

import java.awt.Point;
import static java.lang.Double.NaN;

/**
 *
 * @author pelzinga010800
 */
public class Collision {
    Collision(){}
    
//    public boolean checkCollisionBoxes(int blx1A, int bly1A, int blx2A, int bly2A, int blx1B, int bly1B, int blx2B, int bly2B){
//        if(bly1A > bly1B && bly2A < bly2B && blx1A > blx1B && blx2A < blx2B){
//            return true;
//        }else{
//            return false;
//        }
//    }
    public boolean checkCollisionBoxes(int blx1A, int bly1A, int blx2A, int bly2A, int blx1B, int bly1B, int blx2B, int bly2B){
        if(bly2A > bly1B && bly1A < bly2B && blx2A > blx1B && blx1A < blx2B){
            return true;
        }else{
            return false;
        }
    }
    
    public Point checkLineCollision(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
        double m1 = (y2 - y1)/(x2 - x1);
        double m2 = (y4 - y3)/(x4 - x3);
        double b1 = 0, b2 = 0, x, y; //x is where they intersect
        
        //Determine terms of x or terms of y
        if(m1 == Double.POSITIVE_INFINITY || m2 == Double.POSITIVE_INFINITY){
            //Terms of y
            b1 = x1;
            m1 = 1/m1;
            b2 = x3;
            m2 = 1/m2;
            
            y = ((b2 - b1)/(m1 - m2));
            x = ((m1 * y) + b1);
            
            y += y1;
            
            if(y3 < y && y4 > y){
                return new Point((int)x, (int)y);
            }else{
                return null;
            }
            
        }else{
            //Terms of x
            b1 = y1;
            b2 = y3;
            
            x = ((b2 - b1)/(m1 - m2));
            y = ((m1 * x) + b1);
            
            x += x1;
            
            if(x3 < x && x4 > x){
                return new Point((int)x, (int)y);
            }else{
                return null;
            }
        }
    }
    
    public boolean checkPlayerCollision(Player player, int blx1, int bly1, int blx2, int bly2){
        if(player.getY() + 10 > bly1 && player.getY() - 10 < bly2 && player.getX() + 10 > blx1 && player.getX() - 10 < blx2){
            return true;
        }else{
            return false;
        }
    }
    //Actually not even sure what the point of this method is
    public boolean blockCollision(Player player, int blx1A, int bly1A, int blx2A, int bly2A, int blx1B, int bly1B, int blx2B, int bly2B){ //Block collision with entities
        double colDis[] = new double[4], closest = 100;
        int closestCol = -1;
		
        if(bly1A > bly1B && bly2A < bly2B && blx1A > blx1B && blx2A < blx2B){
            colDis[0] = blx1A - blx1B; 
            colDis[1] = bly1A - bly1B;
            colDis[2] = blx2A - blx2B;
            colDis[3] = bly2A - bly2B;
            
            for (int i = 0; i < 4; i++) {
                if(colDis[i] < 0){
                    colDis[i] *= -1;
                }
                
                if(colDis[i] < closest){
                    closest = colDis[i];
                    closestCol = i;
                }
            }
			
            if(closestCol == 0){
                player.setDx(0);
		player.setX(blx1B - 20);
            }else if(closestCol == 1){
                player.setDy(0);
		player.setY(bly1B - 20);
            }else if(closestCol == 2){
		player.setDx(0);
		player.setX(blx2B);
            }else if(closestCol == 3){
                player.setDy(0);
		player.setY(bly2B);
            }
            return true;
        }else{
            return false;
        }
    }
    public boolean blockCollision(Player player, int blx1, int bly1, int blx2, int bly2){ //Block collision with entities
        double colDis[] = new double[4],closest = 100;
        int closestCol = -1;
		
        if(player.getY() + 10 > bly1 && player.getY() - 10 < bly2 && player.getX() + 10 > blx1 && player.getX() - 10 < blx2){
            colDis[0] = player.getX() + 10 - blx1; 
            colDis[1] = player.getY() + 10 - bly1;
            colDis[2] = player.getX() - 10 - blx2;
            colDis[3] = player.getY() - 10 - bly2;
            
            for (int i = 0; i < 4; i++) {
                if(colDis[i] < 0){
                    colDis[i] *= -1;
                }
                
                if(colDis[i] < closest){
                    closest = colDis[i];
                    closestCol = i;
                }
            }
			
            if(closestCol == 0){
                player.setDx(0);
		player.setX(blx1 - 10);
            }else if(closestCol == 1){
                player.setDy(0);
		player.setY(bly1 - 10);
            }else if(closestCol == 2){
		player.setDx(0);
		player.setX(blx2 + 10);
            }else if(closestCol == 3){
                player.setDy(0);
		player.setY(bly2 + 10);
            }
            return true;
        }else{
            return false;
        }
    }
}

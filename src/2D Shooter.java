/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gpk.tower.defense;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Double.NaN;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

/**
 *
 * @author pelzinga010800
 */


//We have two instances of the game classs.... bad


//http://gamedev.stackexchange.com/questions/81733/java2d-shooting-in-a-line-tangent-to-mouse-coordinates
class Window extends JFrame{
    public Window(Game game){
        initWindow(game);
    }
    private void initWindow(Game game){
        add(game);
        
        setVisible(true);
        setSize(656, 518);
        
        addComponentListener(new ResizeListener());
        
        setTitle("");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
            new ImageIcon("Cursor2.png").getImage(),
            new Point(15,15),"custom cursor"));
        
        game.start();
    }
    class ResizeListener implements ComponentListener {

        @Override
        public void componentHidden(ComponentEvent e) {}
        @Override
        public void componentMoved(ComponentEvent e) {}
        @Override
        public void componentShown(ComponentEvent e) {}

        @Override
        public void componentResized(ComponentEvent e) {
//            Dimension newSize = e.getComponent().getBounds().getSize();
//            if(newSize.height > newSize.width){
//                setSize(newSize.height, newSize.height);
//            }else{
//                setSize(newSize.width, newSize.width);
//            }
            
        }   
    }
}

//extends JPanel or extend JFrame
class Game extends JPanel implements Runnable{
    Scanner input = new Scanner(System.in);
    Thread thread;
    boolean running, paused, isKeyPressed;
    boolean keysPressed[];
    char keysPressedOrder[];
    int keysTop;
    int scene;
    int enemyDifficulty;
    float fadeMultiplier;
    double fadeX, fadeY;
    Font defaultFont;
    String fadeText;
    Dimension size;
    Bullet bullet;
    Point bulletCollision;
    Player player;
    Enemy enemy[];
    Structure mapBorder[];
    Collision collision;
    Line line[];
    Gun starterGun;
    Point mouseCords;
    Structure entities[];
    Structure lockedPassage[];
    
    Game(){
        addComponentListener(new ResizeListener());
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        collision = new Collision();
        
        addKeyListener(new TAdapter());
        addMouseMotionListener(new MAdapter());
        addMouseListener(new MAdapter());
        //initGame();
        
    }
    private void initGame(){
        //running = true;
        enemyDifficulty = 10;
        fadeMultiplier = 0;
        defaultFont = new Font("TimesRoman", Font.PLAIN, 14);
        
        starterGun = new Gun(10, 10, 1000, 125, 1);
        player = new Player(starterGun);
        buildMap();
        buildEntities();
        enemy = new Enemy[8];
        //enemy[0] = new Enemy(450, 300, 50, 1, 10);
        keysPressed = new boolean[4];
        keysPressedOrder = new char[4];
        for (int i = 0; i < 4; i++) {
            keysPressedOrder[i] = ' ';
        }
        mouseCords = new Point(0,0);
        
        
    }
    public void start(){
        running = true;
        paused = false;
//        thread = new Thread(this);
//        thread.start(); //Runs the run method
    }
    
    @Override
    public void run (){
        initGame();
        gameLoop();
    }
    private void gameLoop(){
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            if(!paused){
                while(delta >= 1){
                        tick();
                        updates++;
                        delta--;
                }
                render();
                frames++;

                if(System.currentTimeMillis() - timer > 1000){
                        timer += 1000;
                        //System.out.println("FPS: " + frames + " TICKS: " + updates);
                        frames = 0;
                        updates = 0;
                }
            }else if(delta >= 1){
                delta--;
            }
        }
    }
    class Listener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae) {
            //running = false;
        }
    }
    private void tick(){
        mouseCords.x = MouseInfo.getPointerInfo().getLocation().x - Game.this.getLocationOnScreen().x;
        mouseCords.y = MouseInfo.getPointerInfo().getLocation().y - Game.this.getLocationOnScreen().y;
        keyPresses();
        player.move();
        for (int i = 0; i < enemy.length; i++) {
            if(enemy[i] != null && enemy[i].timeToMove <= System.currentTimeMillis()){ //current time has past the required time to move again
                enemy[i].move(player);
            }
        }
        if(fadeMultiplier > 0){
            fadeMultiplier -= 0.01;
            fadeY -= 0.5;
        }
        
        playerCollision();
        spawnEnemies();
        
//        if(bullet != null){
//            bullet.fade();
//        }
        if(player.timeToRegen < System.currentTimeMillis() && player.health < player.maxHealth){
            player.health += player.regenPerk;
            player.timeToRegen = System.currentTimeMillis() + 1000;
        }
        if(player.health <= 0){
            running = false;
            System.out.println("Game over");
            //paused = true;
            scene = 4;
        }
        
    }
    private void playerCollision(){
        for (int i = 0; i < mapBorder.length; i++) {
            if(mapBorder[i] != null){
                collision.blockCollision(player, mapBorder[i].x, mapBorder[i].y, mapBorder[i].width + mapBorder[i].x, mapBorder[i].height + mapBorder[i].y);
            } 
       }
        for (int i = 0; i < lockedPassage.length; i++) {
            if(lockedPassage[i] != null){
                collision.blockCollision(player, lockedPassage[i].x, lockedPassage[i].y, lockedPassage[i].width + lockedPassage[i].x, lockedPassage[i].height + lockedPassage[i].y);
                if(collision.checkPlayerCollision(player, lockedPassage[i].x - 10, lockedPassage[i].y, lockedPassage[i].x + lockedPassage[i].width, lockedPassage[i].y + lockedPassage[i].height)){
                    player.nearPassage[i] = true;
                    break;
                }else{
                    player.nearPassage[i] = false;
                }
            } 
        }
        for (int i = 0; i < enemy.length; i++) {
            if(enemy[i] != null){
                if(collision.blockCollision(player, (int)enemy[i].x, (int)enemy[i].y, (int)enemy[i].x + 20, (int)enemy[i].y + 20) && enemy[i].timeToAttack <= System.currentTimeMillis()){
                    player.health -= enemy[i].damage;
                    enemy[i].timeToAttack = (long)(System.currentTimeMillis() + enemy[i].atckCoolDown);
                    if(player.health <= 0){
                        System.out.println("Enemy Difficulty: " + enemyDifficulty);
                        System.out.println("Health: " + enemy[i].health);
                        System.out.println("Damage: " + enemy[i].damage);
                        System.out.println("Speed: " + enemy[i].speed);
                    }
                }
            }
        }
        for (int i = 0; i < entities.length; i++) {
            if(entities[i] != null){
                if(collision.checkPlayerCollision(player, entities[i].x, entities[i].y, entities[i].x + entities[i].width, entities[i].y + entities[i].height)){
                    player.onEntity[i] = true;
                    break;
                }else{
                    player.onEntity[i] = false;
                }
            }
        }
    }
    private void keyPresses(){
        int aFound = -1, dFound = -1, sFound = -1, wFound = -1;
        char calcKeys[];
        calcKeys = new char[2];
        calcKeys[0] = ' ';
        calcKeys[1] = ' ';
        
        for (int i = 0; i < 4; i++) {
            if(keysPressedOrder[i] == 'a'){
                aFound = i;
            }else if(keysPressedOrder[i] == 'd'){
                dFound = i;
            }else if(keysPressedOrder[i] == 'w'){
                wFound = i;
            }else if(keysPressedOrder[i] == 's'){
                sFound = i;
            }
        }
        if(aFound < dFound){
            //player.calcSpeed('d', mouseCords);
            calcKeys[0] = 'd';
        }else if(aFound > dFound){
            //player.calcSpeed('a', mouseCords);
            calcKeys[0] = 'a';
        }
        if(wFound < sFound){
            //player.calcSpeed('s', mouseCords);
            calcKeys[1] = 's';
        }else if(wFound > sFound){
            //player.calcSpeed('w', mouseCords);
            calcKeys[1] = 'w';
        }
        player.calcSpeed(calcKeys, mouseCords);
        if(!keysPressed[0] && !keysPressed[1]){
            player.dx = 0;
        }
        if(!keysPressed[2] && !keysPressed[3]){
            player.dy = 0;
        }
//        if(keysPressed[0] && !keysPressed[1]){
//            player.dx = -5;
//        }else if(!keysPressed[0] && keysPressed[1]){
//            player.dx = 5;
//        }
    }
    private void render(){
        repaint();
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(scene == 0){
            drawTitleScene(g);
        }else if(scene == 1){
            drawGameScene(g);
        }else if(scene == 2){
            drawGameScene(g);
            drawPauseScene(g);
        }else if(scene == 3){
            drawMysteryGunScene(g);
        }else if(scene == 4){
            drawGameOverScene(g);
        }
        //drawGameScene(g);
    }
    private void drawGameScene(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, 
            RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        
        AffineTransform newAT = AffineTransform.getScaleInstance((double)size.width/640, (double)size.height/480);
        AffineTransform saveAT = g2d.getTransform();
        g2d.transform(newAT);
        
        g.translate(-(int)player.getX() + 320, -(int)player.getY() + 240);
        
        drawMap(g);
        drawPlayer(g);
        drawEnemies(g);
        drawHealthBar(g);
        if(bullet != null){
            drawBullet(g);
        }
        //drawBoxes(g);
        drawGunStatus(g);
        
        if(player.onEntity[0]){
            drawGunQuery(g);
        }else if(player.onEntity[1]){
            drawSpeedPerkQuery(g);
        }else if(player.onEntity[2]){
            drawMysteryBoxQuery1(g);
        }else if(player.onEntity[3]){
            drawHealthPerkQuery(g);
        }else if(player.onEntity[4]){
            drawRegenPerkQuery(g);
        }else if(player.onEntity[5]){
            drawMysteryBoxQuery2(g);
        }
        if(player.nearPassage[0]){
            drawPassageQuery1(g);
        }else if(player.nearPassage[1]){
            drawPassageQuery2(g);
        }
        drawFadeText(g);
        
        if(!running){
            //scene = some number, the game over screen
            g.setColor(Color.WHITE);
            g.drawString("Game Over", (int)player.getX() - 25, (int)player.getY() - 50);
        }
        g2d.setTransform(saveAT);
    }
    private void drawTitleScene(Graphics g){
        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        g.drawString("Shooter", 270, 80);
        
        
        g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        
        g.setColor(Color.GRAY);
        g.fillRect(270, 230, 100, 20);
        g.setColor(Color.CYAN);
        g.drawString("New Game", 290, 244);
        
        g.setColor(Color.GRAY);
        g.fillRect(270, 257, 100, 20);
        g.setColor(Color.CYAN);
        g.drawString("Load", 305, 271);
        
    }
    private void drawPauseScene(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(Color.yellow);
        //g.drawString("Paused", 300, 240);
        
//        g.setColor(Color.WHITE);
//        g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
//        g.drawString("Paused", 280, 130);
//        g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)0.3));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 640, 480);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)1));
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        g.drawString("Paused", 280, 130);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        
        g.setColor(new Color(62, 144, 149));
        g.fillRect(220, 260, 80, 25);
        g.fillRect(340, 260, 80, 25);
        g.fillRect(280, 305, 80, 25);
        
        g.setColor(Color.WHITE);
        g.drawString("Resume", 232, 277);
        g.drawString("Menu", 362, 277);
        g.drawString("Save", 302, 322);
        
        
    }
    private void drawMysteryGunScene(Graphics g){
        g.setColor(new Color(53, 53, 53));
        g.fillRect(120, 90, 400, 300);
        g.setColor(Color.WHITE);
        double damage, piercing, reloadTime;
        
        damage = Math.round(player.mysteryGun.damage * 10);
        piercing = Math.round(player.mysteryGun.piercing * 10);
        reloadTime = Math.round(player.mysteryGun.reloadTime/10); //For rounding purposes
        
        g.drawString("Mystery Gun Bought", 265, 120);
        
        g.drawString("New Gun", 155, 160);
        g.drawString("Damage: " + damage/10, 140, 180);
        g.drawString("Fire Rate: " + 1000/player.mysteryGun.fireRate, 140, 200);
        g.drawString("Piercing: " + piercing/10, 140, 220);
        g.drawString("Clip Size: " + player.mysteryGun.clipSize, 140, 240);
        g.drawString("Reload: " + reloadTime/100, 140, 260);
        
        damage = Math.round(player.currGun.damage * 10);
        piercing = Math.round(player.currGun.piercing * 10);
        reloadTime = Math.round(player.currGun.reloadTime/10);
        
        g.drawString("Current Gun", 410, 160);
        g.drawString("Damage: " + damage/10, 410, 180);
        g.drawString("Fire Rate: " + 1000/player.currGun.fireRate, 410, 200);
        g.drawString("Piercing: " + piercing/10, 410, 220);
        g.drawString("Clip Size: " + player.currGun.clipSize, 410, 240);
        g.drawString("Reload: " + reloadTime/100, 410, 260);
        
        g.drawString("Equip Mystery Gun?", 265, 300);
        
        g.setColor(Color.GREEN);
        g.fillRect(255, 330, 45, 25);
        g.setColor(Color.MAGENTA);
        g.drawRect(255, 330, 45, 25);
        g.setColor(Color.BLACK);
        g.drawString("Equip", 264, 347);
        
        g.setColor(new Color(225, 0, 0));
        g.fillRect(325, 330, 45, 25);
        g.setColor(Color.MAGENTA);
        g.drawRect(325, 330, 45, 25);
        g.setColor(Color.BLACK);
        g.drawString("Discard", 327, 347);
    }
    private void drawGameOverScene(Graphics g){
        g.setColor(Color.WHITE);
        //Game over text in this location, perhaps in bigger font size
        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        g.drawString("Game Over", 270, 100);
        
        g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
        
        g.drawString("Kills: " + player.kills, 100, 150);
        String timeMin = Long.toString(((System.currentTimeMillis() - player.creationTime)/1000)/60);
        String timeSec = Long.toString(((System.currentTimeMillis() - player.creationTime)/1000) % 60);
        if(timeSec.length() == 1){
            timeSec = "0" + timeSec;
        }
        //g.drawString("Time: " + ((System.currentTimeMillis() - player.creationTime)/1000) + " seconds", 100, 170); //Convert to Min:Sec
        g.drawString("Time: " + timeMin + ":" + timeSec, 100, 170);
        
        g.setColor(Color.GREEN);
        g.fillRect(280, 375, 80, 30);
        g.setColor(Color.MAGENTA);
        g.drawRect(280, 375, 80, 30);
        g.setColor(Color.BLACK);
        g.drawString("Main Menu", 290, 395);
    }
    private void drawPlayer(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        
        AffineTransform transform = new AffineTransform();
        AffineTransform saveAT = g2d.getTransform();
        
        transform.rotate(player.calcAngle(mouseCords), player.getX(), player.getY());
        g2d.transform(transform);
        //g.setColor(Color.RED);
        //g.fillRect((int)player.getX(), (int)player.getY(), 20, 20);
        BufferedImage image = loadImage("Player Sprite.png");
        g.drawImage(image, (int)(player.getX() - 18), (int)player.getY() - 8, null);
        //g.drawLine((int)player.getX(), (int)player.getY(), (int)player.getX() + 10, (int)player.getY());
        //g.setColor(Color.WHITE);
        //g.drawLine((int)player.getX() - 10, (int)player.getY(), (int)player.getX(), (int)player.getY());
        g2d.setTransform(saveAT);
        g.setColor(Color.WHITE);
        //g.setFont(new Font("Arial", Font.PLAIN, 12));
        //g.drawString("Health: " + Double.toString(Math.round(player.health)), (int)player.getX() - 300, (int)player.getY() - 220);
        g.drawString("Health: ", (int)player.getX() - 300, (int)player.getY() - 220);
    }
    private void drawBullet(Graphics g){
        int x, y;
        x = ((int)player.getX()) - 320;
        //x = ((int)player.getX()) - size.width/2;
        y = ((int)player.getY()) - 240;
        //y = ((int)player.getY()) - size.height/2;

        g.setColor(Color.WHITE);
        //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bullet.transparency));
        if(bulletCollision != null){
            g.drawLine((int)player.getX(), (int)player.getY(), bullet.x , bullet.y);
            //g.drawLine(bullet.x1, bullet.y1, bullet.x2 , bullet.y2); //Point one will not follow player when bullet is shot
            //Not sure which one looks better
        }else{
            g.drawLine((int)player.getX(), (int)player.getY(), bullet.x + x , bullet.y  + y);
        }

        if(bullet.finalTime <= System.currentTimeMillis()){
            bullet = null;
        }
    }
    private void drawMap(Graphics g){
        g.setColor(Color.BLACK);
        for (int i = 0; i < mapBorder.length; i++) {
            if(mapBorder[i] != null){
                g.fillRect(mapBorder[i].x, mapBorder[i].y, mapBorder[i].width, mapBorder[i].height);
            }
        }
        for (int i = 0; i < lockedPassage.length; i++) {
            if(lockedPassage[i] != null){
                g.fillRect(lockedPassage[i].x, lockedPassage[i].y, lockedPassage[i].width, lockedPassage[i].height);
            }
        }
        g.setColor(Color.GRAY);
        g.fillRect(100, 100, 490, 330);
        
        for (int i = 0; i < entities.length; i++) {
            if(entities[i] != null){
                g.setColor(Color.BLUE);
                if(i == 5){
                    g.setColor(new Color(168, 0, 0));
                }
                g.fillRect(entities[i].x, entities[i].y, entities[i].width, entities[i].height);
            }
        }
        
        g.setColor(Color.WHITE);
        g.drawString("Buy", entities[0].x + 13, entities[0].y + 20);
        g.drawString("Gun", entities[0].x + 13, entities[0].y + 35);
        g.drawString("Perk", entities[1].x + 13, entities[1].y + 20);
        g.drawString("Speed", entities[1].x + 10, entities[1].y + 35);
        g.drawString("Mystery",entities[2].x + 5, entities[2].y + 20);
        g.drawString("Box", entities[2].x + 15, entities[2].y + 35);
        g.drawString("Perk", entities[3].x + 13, entities[3].y + 20);
        g.drawString("Health", entities[3].x + 10, entities[3].y + 35);
        g.drawString("Perk", entities[4].x + 13, entities[4].y + 20);
        g.drawString("Regen", entities[4].x + 10, entities[4].y + 35);
        g.drawString("Mystery",entities[5].x + 5, entities[5].y + 20);
        g.drawString("Box", entities[5].x + 15, entities[5].y + 35);
    }
    private void drawEnemies(Graphics g){
        for (int i = 0; i < enemy.length; i++) {
            if(enemy[i] != null){
                g.setColor(new Color(enemy[i].r, enemy[i].b, enemy[i].g));
                g.fillRect((int)enemy[i].x, (int)enemy[i].y, 20, 20);
                g.setColor(Color.WHITE);
                //g.drawString(Double.toString(Math.round(enemy[i].health)), (int)enemy[i].x, (int)enemy[i].y);
            }
        }
    }
    private void drawGunStatus(Graphics g){
        g.setColor(Color.WHITE);
        if(player.currGun.reloadFinish >= System.currentTimeMillis()){
            g.drawString("Clip: ", (int)player.getX() - 250, (int)player.getY() - 200);
            g.drawString("Reloading", (int)player.getX() - 250, (int)player.getY() - 180);
        }else{
            g.drawString("Clip: " + player.currGun.currClip, (int)player.getX() - 250, (int)player.getY() - 200);
        }
        g.drawString("Kills: " + player.kills, (int)player.getX() - 180, (int)player.getY() - 200);
        g.drawString("Money: " + player.money, (int)player.getX() - 100, (int)player.getY() - 200);
    }
    private void drawHealthBar(Graphics g){
        g.setColor(Color.RED);
        g.drawRect((int)player.getX() - 261, (int)player.getY() - 231, 201, 16);
        g.setColor(Color.GREEN);
        //(player.health/player.maxHealth) * 2
        if((int)(player.health/player.maxHealth * 200) > 0){
            g.fillRect((int)player.getX() - 260, (int)player.getY() - 230, (int)(player.health/player.maxHealth * 200), 15);
        }
    }
    private void drawGunQuery(Graphics g){
        g.drawString("Buy a new gun for $100?", (int)player.getX() - 60, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawSpeedPerkQuery(Graphics g){
        if(player.speedPerk == 0){
            g.drawString("Purchase Speed Perk Level 1?", (int)player.getX() - 80, (int)player.getY() - 100);
            drawButton(g);
        }else if(player.speedPerk == 1){
            g.drawString("Purchase Speed Perk Level 2?", (int)player.getX() - 80, (int)player.getY() - 100);
            drawButton(g);
        }
    }
    private void drawMysteryBoxQuery1(Graphics g){
        g.drawString("Pay $100 to use the Mystery Box?", (int)player.getX() - 80, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawMysteryBoxQuery2(Graphics g){
        g.drawString("Pay $500 to use the Mystery Box?", (int)player.getX() - 80, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawHealthPerkQuery(Graphics g){
        g.drawString("Pay $100 to increase your health?", (int)player.getX() - 80, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawRegenPerkQuery(Graphics g){
        g.drawString("Pay $100 to regen health?", (int)player.getX() - 60, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawPassageQuery1(Graphics g){
        g.drawString("Pay $200 to unlock the passage?", (int)player.getX() - 80, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawPassageQuery2(Graphics g){
        g.drawString("Pay $500 to unlock the passage?", (int)player.getX() - 80, (int)player.getY() - 100);
        drawButton(g);
    }
    private void drawFadeText(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        if(fadeMultiplier > 0){
            //FontMetrics metrics = g.getFontMetrics(defaultFont);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeMultiplier));
            g.setColor(Color.WHITE);
            g.drawString(fadeText, (int)fadeX, (int)fadeY);
            
        }
        //(int)player.getX() - fadeText.length/2  --Something to center the text
        
        
    }
    private void drawButton(Graphics g){
        g.setColor(Color.GREEN);
        g.fillRect((int)player.getX() - 20, (int)player.getY() + 70, 40, 30);
        g.setColor(Color.MAGENTA);
        g.drawRect((int)player.getX() - 20, (int)player.getY() + 70, 40, 30);
        g.setColor(Color.BLACK);
        g.drawString("Yes", (int)player.getX() - 11, (int)player.getY() + 90);
    }
    
    private void buildMap(){
        mapBorder = new Structure[20];
        
        mapBorder[0] = new Structure(0, 430, 690, 100);
        mapBorder[1] = new Structure(0, 0, 100, 530);
        mapBorder[2] = new Structure(0, 0, 1440, 100);
        mapBorder[3] = new Structure(1340, 0, 100, 700);
        mapBorder[4] = new Structure(540, 430, 100, 620);
        mapBorder[5] = new Structure(540, 900, 900, 100);
        mapBorder[6] = new Structure(1340, 850, 300, 100);
        mapBorder[7] = new Structure(1440, 650, 300, 100);
        mapBorder[8] = new Structure(1740, 650, 100, 300);
        mapBorder[9] = new Structure(1840, 850, 300, 100);
        mapBorder[10] = new Structure(2040, 950, 100, 200);
        mapBorder[11] = new Structure(1440, 950, 100, 200);
        mapBorder[12] = new Structure(1440, 1150, 700, 100);
        
        lockedPassage = new Structure[5];
        
        lockedPassage[0] = new Structure(590, 0, 100, 480);
        lockedPassage[1] = new Structure(1340, 700, 100, 150);
    }
    private void buildEntities(){
        entities = new Structure[6];
        
        entities[0] = new Structure(100, 100, 50, 50);
        entities[1] = new Structure(100, 200, 50, 50);
        entities[2] = new Structure(800, 800, 50, 50);
        entities[3] = new Structure(1290, 300, 50, 50);
        entities[4] = new Structure(1290, 400, 50, 50);
        entities[5] = new Structure(1900, 1100, 50, 50);
    }
    private void drawBoxes(Graphics g){
        g.setColor(Color.WHITE);
        
        double x1 = player.getX();
        double y1 = player.getY();
        
        g.drawRect((int)x1, (int)y1, -320, 240);
        
        int x3 = mapBorder[0].x;
        int y3 = mapBorder[0].y;
        int x4 = mapBorder[0].width;
        int y4 = mapBorder[0].height;
        
        g.setColor(Color.ORANGE);
        g.drawRect(x3, y3, x4, y4);
    }
    private void spawnEnemies(){
        Random rand = new Random();
        double rndHealth, rndDamage, rndSpeed;
        int x, y;
        for (int i = 0; i < enemy.length; i++) {
            rndHealth = (double)(rand.nextFloat() * enemyDifficulty);
            rndDamage = (double)(rand.nextFloat() * (enemyDifficulty - rndHealth));
            rndSpeed = enemyDifficulty - rndHealth - rndDamage;
            x = ThreadLocalRandom.current().nextInt((int)player.getX() - 350, (int)player.getX() + 350 + 1);
            y = ThreadLocalRandom.current().nextInt((int)player.getY() - 350, (int)player.getY() + 350 + 1);
            while((x < player.getX() + 100 && x > player.getX() - 100) || (y < player.getY() + 100 && y > player.getY() - 100)){
                x = ThreadLocalRandom.current().nextInt((int)player.getX() - 350, (int)player.getX() + 350 + 1);
                y = ThreadLocalRandom.current().nextInt((int)player.getY() - 350, (int)player.getY() + 350 + 1);
            }
            
            if(enemy[i] == null){
                rndHealth = (rndHealth * 10) + 5;
                rndDamage = (rndDamage * 4) + 3;
                rndSpeed = (rndSpeed * 0.1) + 0.3; //Make it some kind of a square root relationship
                if(rndSpeed > 1){
                    rndSpeed = Math.pow(rndSpeed, 0.5);
                }
                enemy[i] = new Enemy(x, y, rndHealth, rndSpeed, rndDamage);
                //enemy[i] = new Enemy(450, 300, 50, 1, 10);
            }
        }
    }
    private BufferedImage loadImage(String fileName){
        BufferedImage image = null;
        
        try {
            image = ImageIO.read(new File(fileName));
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }
    class ResizeListener implements ComponentListener {
        @Override
        public void componentHidden(ComponentEvent e) {}
        @Override
        public void componentMoved(ComponentEvent e) {}
        @Override
        public void componentShown(ComponentEvent e) {}

        @Override
        public void componentResized(ComponentEvent e) {
            Dimension newSize = e.getComponent().getBounds().getSize();
            size = newSize;
            System.out.println(size.width);
            System.out.println(size.height);
        }   
    }
    private class TAdapter extends KeyAdapter{
        public TAdapter() {
            
        }
        @Override
        public void keyReleased(KeyEvent e){
            int key = e.getKeyCode();
            
            if (key == KeyEvent.VK_A) {
                findChar('a');
                keysPressed[0] = false;
            }

            if (key == KeyEvent.VK_D) {
                findChar('d');
                keysPressed[1] = false;
            }

            if (key == KeyEvent.VK_W) {
                findChar('w');
                keysPressed[2] = false;
            }

            if (key == KeyEvent.VK_S) {
                findChar('s');
                keysPressed[3] = false;
            }
            
            
        }
        @Override
        public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();
            
            if (key == KeyEvent.VK_A) {
                if(!keysPressed[0]){
                    keysPressedOrder[keysTop] = 'a';
                    keysTop++;
                }
                keysPressed[0] = true;
            }

            if (key == KeyEvent.VK_D) {
                if(!keysPressed[1]){
                    if(keysTop == -1){
                    }
                    keysPressedOrder[keysTop] = 'd';
                    keysTop++;
                }
                keysPressed[1] = true;
            }

            if (key == KeyEvent.VK_W) {
                if(!keysPressed[2]){
                    keysPressedOrder[keysTop] = 'w';
                    keysTop++;
                }
                keysPressed[2] = true;
            }

            if (key == KeyEvent.VK_S) {
                if(!keysPressed[3]){
                    keysPressedOrder[keysTop] = 's';
                    keysTop++;
                }
                keysPressed[3] = true;
            }
            if (key == KeyEvent.VK_ESCAPE && scene != 0){ //Pauses game
                paused = !paused;
                if(scene == 1){
                    scene = 2;
                }else if(scene == 2){
                    scene = 1;
                }
            }
            if (key == KeyEvent.VK_R && player.currGun.currClip != player.currGun.clipSize){
                player.currGun.reloadFinish = System.currentTimeMillis() + player.currGun.reloadTime;
                player.currGun.currClip = player.currGun.clipSize;
            }
            if (key == KeyEvent.VK_E && player.gun2 != null){
                if(player.currGun == player.gun1){
                    player.currGun = player.gun2;
                }else if(player.currGun == player.gun2){
                    player.currGun = player.gun1;
                }
            }
            if (key == KeyEvent.VK_H){
                saveToFile();
            }
            if (key == KeyEvent.VK_G){
                openFile("Save File.txt");
            } 
        }
        private void findChar(char c){
            for (int i = 0; i < 3; i++) {
                if(keysPressedOrder[i] == c){
                    keysPressedOrder[i] = keysPressedOrder[i + 1];
                }
            }
            keysTop--;
        }
    }
    public class MAdapter extends MouseAdapter{
        long initTime = -1, finalTime = -1;
        //Point mouseCords;
        Timer timer;
        TimerTask task;
        @Override
        public void mouseClicked(MouseEvent e){
            
//            Point shot;
//            shot = MouseInfo.getPointerInfo().getLocation();
//            //System.currentTimeMillis();
//            bullet = new Bullet(shot.x, shot.y, System.currentTimeMillis()); //milliseconds or nanoseconds 
                //player.calcAngle(mouseCords);
        }
        private void setFadeText(String text){
            fadeText = text;
            fadeX = (int)player.getX();
            fadeY = (int)player.getY();
            fadeMultiplier = 1;
        }
        @Override
        public void mousePressed(MouseEvent e){
            //semi auto implemented
//            if(initTime == -1){ //This is just for the first bullet shot, perhaps a better solution is in order
//                buildBullet(e);
//                initTime = System.currentTimeMillis();
//                finalTime = initTime + 100;
//                initTime = 0;
//                player.currGun.currClip--;
//            }
//            if(finalTime <= System.currentTimeMillis() && player.currGun.reloadFinish <= System.currentTimeMillis()){
//                buildBullet(e);
//                finalTime = System.currentTimeMillis() + 100;
//                player.currGun.currClip--;
//                if(player.currGun.currClip == 0){
//                //reloading
//                    player.currGun.reloadFinish = System.currentTimeMillis() + player.currGun.reloadTime;
//                    player.currGun.currClip = player.currGun.clipSize;
//                    System.out.println("Reloading");
//                }
//            }
            Point click = e.getPoint();
            
            //g.fillRect((int)player.getX() - 20, (int)player.getY() + 60, 40, 30);
            if(scene == 0){ //g.fillRect(270, 230, 100, 20);
                if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 270, 230, 370, 250)){
                    scene = 1;
                    thread = new Thread(Game.this);
                    thread.start(); //Runs the run method
                    running = true;
                }else if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 270, 257, 370, 277)){
                    scene = 1;
                    thread = new Thread(Game.this);
                    thread.start(); //Runs the run method
                    running = true;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    openFile("Save File.txt");
                }
            }else if(scene == 1){
                click.x = (int)(click.x + player.getX() - 320);
                click.y = (int)(click.y + player.getY() - 240);
                if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 
                        (int)player.getX() - 20, (int)player.getY() + 60, (int)player.getX() + 20, (int)player.getY() + 100)){
                    System.out.println("clicked");
                    if(player.onEntity[0]){
                        if(player.currGun.ID != 1){
                            if(player.money >= 100){
                                System.out.println("You bought a new gun!");
                                player.currGun = new Gun(15, 15, 1250, 120, 2);
                                player.gun2 = player.currGun;
                                player.money -= 100;
                                setFadeText("Gun Bought");
                            }else{
                                System.out.println("Not enough money.");
                                setFadeText("Insufficient Funds");
                            }
                        }else{
                            System.out.println("You already have this gun.");
                            setFadeText("Already Bought");
                        }
                    }else if(player.onEntity[1]){
                        if(player.speedPerk == 0){
                            if(player.money >= 100){
                                System.out.println("Purchased Speed Perk Level 1");
                                player.speedPerk = 1;
                                player.speed = 1.25;
                                player.money -= 100;
                                setFadeText("Purchased Speed Perk Level 1");
                            }
                        }else if(player.speedPerk == 1){
                            if(player.money >= 200){
                                System.out.println("Purchased Speed Perk Level 2");
                                player.speedPerk = 2;
                                player.speed = 1.5;
                                player.money -= 200;
                                setFadeText("Purchased Speed Perk Level 2");
                            }
                        }else{
                            setFadeText("Speed Maxed");
                        }
                    }else if(player.onEntity[2]){
                        if(player.money >= 100){
                            int rndNum = ThreadLocalRandom.current().nextInt(7, 13 + 1);
                            
                            buildRndGun(rndNum);
                            player.money -= 100;
                        }
                        //Implement a cooldown for the mystery box?
                    }else if(player.onEntity[3]){
                        if(player.healthPerk == 0){
                            if(player.money >= 100){
                                player.maxHealth += 100;
                                player.health += 100;
                                player.money -= 100;
                                player.healthPerk = 1;
                                setFadeText("Purchased Health Perk Level 1");
                                fadeMultiplier = 1;
                            }
                        }else{
                            setFadeText("Already Bought");
                        }
                    }else if(player.onEntity[4]){
                        if(player.regenPerk == 0){
                            if(player.money >= 100){
                                player.regenPerk = 1;
                                player.money -= 100;
                                setFadeText("Purchased Regen Perk Level 1");
                                fadeMultiplier = 1;
                            }
                        }else{
                            setFadeText("Already Bought");
                        }
                    }else if(player.onEntity[5]){
                        if(player.money >= 300){
                            int rndNum = ThreadLocalRandom.current().nextInt(17, 23 + 1); //Doubled stats
                            
                            buildRndGun(rndNum);
                            player.money -= 300;
                        }
                    }else if(player.nearPassage[0]){
                        if(lockedPassage[0] != null){
                            if(player.money >= 200){
                                lockedPassage[0] = null;
                                player.nearPassage[0] = false;
                                player.money -= 200;
                            }
                        }
                    }else if(player.nearPassage[1]){
                        if(lockedPassage[1] != null){
                            if(player.money >= 500){
                                lockedPassage[1] = null;
                                player.nearPassage[1] = false;
                                player.money -= 500;
                            }
                        }
                    }
                }else{
                    if(e.getButton() == MouseEvent.BUTTON1){
                        timer = new Timer();
                        task = new MyTimerTask();
                        //timer.schedule(task, 0, 1000);
                        timer.scheduleAtFixedRate(task, 0, player.currGun.fireRate);
                    }
                }
            }else if(scene == 2){
                if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 220, 260, 300, 285)){
                    paused = false; //unpause
                    scene = 1;
                }else if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 340, 260, 420, 285)){
                    running = false;
                    scene = 0;
                    repaint();
                    add(new Game());
                    paused = false;
                    //prompt to save game
                }else if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 280, 305, 360, 330)){
                    saveFile("Save File.txt");
                    //Notify user
                }
            }else if(scene == 3){
                if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 255, 330, 310, 355)){
                    if(player.gun2 == null){
                        player.gun2 = player.mysteryGun;
                    }else if(player.currGun == player.gun1){
                        player.gun1 = player.mysteryGun;
                    }else if(player.currGun == player.gun2){
                        player.gun2 = player.mysteryGun;
                    }
                    player.currGun = player.mysteryGun;
                    player.mysteryGun = null;
                    fadeText = "Mystery Gun Equipped";
                    fadeMultiplier = 1;
                    fadeX = (int)player.getX();
                    fadeY = (int)player.getY();
                    scene = 1;
                    paused = false;
                }else if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 325, 330, 370, 355)){
                    player.mysteryGun = null;
                    fadeText = "Mystery Gun Discarded";
                    fadeMultiplier = 1;
                    fadeX = (int)player.getX();
                    fadeY = (int)player.getY();
                    scene = 1;
                    paused = false;
                }
            }else if(scene == 4){
                if(collision.checkCollisionBoxes(click.x, click.y, click.x, click.y, 
                        280, 375, 360, 405)){
                    scene = 0;
                    repaint();
                    add(new Game());
                }
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1 && timer != null){
                timer.cancel();
            }
        }
        @Override
        public void mouseMoved(MouseEvent e){
            //http://gamedev.stackexchange.com/questions/68832/how-can-i-drag-a-polygon-based-on-a-mouse-moved-event
            //java keeping track of mouse cordinates while pressed
        }
        private void buildRndGun(int rawGunValue){
            Random rand = new Random();
            double total;
            double damage, piercing, clipSize, reloadTime, fireRate;
            
            
            damage = (double)(rand.nextFloat() * rawGunValue);
            piercing = (double)(rand.nextFloat() * rawGunValue);
            clipSize = (double)(rand.nextFloat() * rawGunValue);
            reloadTime = (double)(rand.nextFloat() * rawGunValue);
            fireRate = (double)(rand.nextFloat() * rawGunValue);
            
            total = damage + piercing + clipSize + reloadTime + fireRate;
            
            damage = damage * rawGunValue/total;
            piercing = piercing * rawGunValue/total;
            clipSize = clipSize * rawGunValue/total;
            reloadTime = reloadTime * rawGunValue/total;
            fireRate = fireRate * rawGunValue/total;
            
            System.out.println("Raw Value: " + rawGunValue);
            System.out.println("damage: " + damage);
            System.out.println("clipSize: " + clipSize);
            System.out.println("reloadTime: " + reloadTime);
            System.out.println("fireRate: " + fireRate);
            System.out.println("piercing: " + piercing);
            System.out.println("");
            
            damage = (damage * 4) + 4;
            piercing = (piercing / 2) + 1;
            clipSize = (clipSize * 10) + 4;
            //reloadTime = ((rawGunValue/2.5 - reloadTime) * 400) + 350; //I'm not sure if I could do something similar to the reload 
            //fireRate = ((rawGunValue/1.5 - fireRate) * 15) + 20; //What if here I was given the rounds per second, and I then converted that to milliseconds
            if(reloadTime < 0.1){
                
            }
            
            reloadTime = ((rawGunValue/2.5 - reloadTime) * 400) + 350;
            //reloadTime = 1000/((reloadTime/3)); //Math.pow
            fireRate = 1000/((fireRate * 4) + 4);
            //player.gun = new Gun(damage, (int)clipSize, (int)reloadTime, (int)fireRate, piercing);
            player.mysteryGun = new Gun(damage, (int)clipSize, (int)reloadTime, (int)fireRate, piercing);
            
            paused = true;
            scene = 3;
            
            System.out.println("damage: " + damage);
            System.out.println("clipSize: " + clipSize);
            System.out.println("reloadTime: " + reloadTime);
            System.out.println("fireRate: " + fireRate);
            System.out.println("piercing: " + piercing);
//            System.out.println("");
//            rndHealth = (double)(rand.nextFloat() * enemyDifficulty);
//            rndDamage = (double)(rand.nextFloat() * (enemyDifficulty - rndHealth));
//            rndSpeed = enemyDifficulty - rndHealth - rndDamage;
            
            
        }
        private class MyTimerTask extends TimerTask{
            @Override
            public void run(){
                //mouseCords = new Point(0,0);
                
                
                if(finalTime <= System.currentTimeMillis() && player.currGun.reloadFinish <= System.currentTimeMillis()){
                    buildBullet(mouseCords);
                    player.currGun.currClip--;
                    if(player.currGun.currClip <= 0){
                    //reloading
                        player.currGun.reloadFinish = System.currentTimeMillis() + player.currGun.reloadTime;
                        player.currGun.currClip = player.currGun.clipSize;
                    }
                }
                //buildBullet(e)
            }
        }
        private void buildBullet(Point e){
            Point shot;
            //shot = e.getPoint();
            shot = e;
            bullet = new Bullet(shot.x, shot.y, System.currentTimeMillis()); //milliseconds or nanoseconds 
            
            int x, y;
            x = ((int)player.getX()) - 320;
            y = ((int)player.getY()) - 240;
            
            double x1, x2, y1, y2;
            x1 = player.getX();
            x2 = bullet.x + x;
            y1 = player.getY();
            y2 = bullet.y + y;
            
            bulletCollision(x1, y1, x2, y2);
        }
        private boolean lineCollision(double x1, double y1, double x2, double y2, int x3, int y3, int x4, int y4){
            Point temp;
            
            temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y3, x4, y4);

            if(temp != null){
                bulletCollision = new Point(0,0);
                bulletCollision = temp;
                bullet.x = bulletCollision.x;
                bullet.y = bulletCollision.y;
//                    bullet.x1 = (int)player.getX();
//                    bullet.y1 = (int)player.getY();
//                    bullet.x2 = bulletCollision.x;
//                    bullet.y2 = bulletCollision.y;
                return true;
            }else{
                //bulletCollision = null;
                return false;
            }
            
        }
        private Point subBulletCollision(double x1, double y1, double x2, double y2, int x3, int y3, int x4, int y4){
            Point temp;
            if(collision.checkCollisionBoxes((int)x1 - 340, (int)y1 - 260, (int)x1 + 340, (int)y1 + 260, x3, y3, x4, y4)){
                //Then there was collision with the screen and a box that is on screen, actually also checks slightly off screen, +20 pixels
                //line[counter] new Line()

                if(x1 < x2 && y1 < y2){ //If the shot is in quadrant 4
                    //Eleminate the outer if then statement that checks if it's on the screen
                    //Instead, we will just check to see if it's in a given quadrant
                    //Within each of these four cases in this if then statement check to see if it is in the bounds of the quadrant
                    //If it is in the quadrant, check collision on the fly, do not add to an array
                    //This would require the code at the top of this method to be in a seperate method
                    if(collision.checkCollisionBoxes((int)x1, (int)y1, (int)x1 + 340, (int)y1 + 260, x3, y3, x4, y4)){
                        temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y3, x4, y3); //horizontal line
                        if(temp != null){ //If temp isn't equal to null there is no reason to check other line
                            return temp;
                        }else{
                            temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y3, x3, y4); //vertical line
                            if(temp != null){
                                return temp;
                            }
                        }
//                            if(lineCollision(x1, y1, x2, y2, x3, y3, x4, y3) || lineCollision(x1, y1, x2, y2, x3, y3, x3, y4)){
//                                System.out.println("Quadrant 4"); //horizontal line then vertical is checked 
//                                
//                                break;
//                            }
                    }
                }else if(x1 < x2 && y1 > y2){ //Quadrant 1
                    if(collision.checkCollisionBoxes((int)x1, (int)y1 - 260, (int)x1 + 340, (int)y1, x3, y3, x4, y4)){
                        temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y4, x4, y4);
                        if(temp != null){
                            return temp;
                        }else{
                            temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y3, x3, y4);
                            if(temp != null){
                                return temp;
                            }
                        }
//                            if(lineCollision(x1, y1, x2, y2, x3, y4, x4, y4) || lineCollision(x1, y1, x2, y2, x3, y3, x4, y3)){
//                                System.out.println("Quadrant 1"); 
//                                break;
//                            }
                    }
                }else if(x1 > x2 && y1 > y2){ //Quadrant 2
                    if(collision.checkCollisionBoxes((int)x1 - 340, (int)y1 - 260, (int)x1, (int)y1, x3, y3, x4, y4)){
                        temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y4, x4, y4);
                        if(temp != null){
                            return temp;
                        }else{
                            temp = collision.checkLineCollision(x1, y1, x2, y2, x4, y3, x4, y4);
                            if(temp != null){
                                return temp;
                            }
                        }
//                            if(lineCollision(x1, y1, x2, y2, x3, y4, x4, y4) || lineCollision(x1, y1, x2, y2, x4, y3, x4, y4)){
//                                System.out.println("Quadrant 2");
//                                break;
//                            }
                    }
                }else if(x1 > x2 && y1 < y2){ //Quadrant 3
                    if(collision.checkCollisionBoxes((int)x1 - 340, (int)y1, (int)x1, (int)y1 + 260, x3, y3, x4, y4)){
                        temp = collision.checkLineCollision(x1, y1, x2, y2, x3, y3, x4, y3);
                        if(temp != null){
                            return temp;
                        }else{
                            temp = collision.checkLineCollision(x1, y1, x2, y2, x4, y3, x4, y4);
                            if(temp != null){
                                return temp;
                            }
                        }
//                            if(lineCollision(x1, y1, x2, y2, x3, y3, x4, y3) || lineCollision(x1, y1, x2, y2, x4, y3, x4, y4)){
//                                System.out.println("Quadrant 3");
//                                break;
//                            }
                    }
                }
            }
            return null;
        }
        private void bulletCollision(double x1, double y1, double x2, double y2){
            int quadrant = 0;
            //Calculation with the 2 points passed as an argument
            int x3, y3, x4, y4;
            int counter = 0;
            int enemyID = -1;
            Point enemyCollision = null;
            //Point collisions[], temp;
            Point temp;
            CollisionSorting collisions[];
            
            collisions = new CollisionSorting[45]; //Make bigger
            
            //new data type: int x, int y, char type, int location
            
            //collisions = new Point[10];
            
            //mapBorder[3] = new Structure((int)(player.getX() - 320), (int)(player.getY() - 240), 640, 480);
            
            
            mapBorder[13] = new Structure((int)player.getX() - 359, (int)player.getY() - 259, 20, 520); //left
            mapBorder[14] = new Structure((int)player.getX() - 339, (int)player.getY() - 279, 680, 20); //top
            mapBorder[15] = new Structure((int)player.getX() - 339, (int)player.getY() + 259, 680, 20); //bottom
            mapBorder[16] = new Structure((int)player.getX() + 339, (int)player.getY() - 259, 20, 520); //right
            
            for (int i = 0; i < mapBorder.length; i++) {
                if(mapBorder[i] != null){
                    x3 = mapBorder[i].x;
                    y3 = mapBorder[i].y;
                    x4 = mapBorder[i].x + mapBorder[i].width;
                    y4 = mapBorder[i].y + mapBorder[i].height;

                    temp = subBulletCollision(x1, y1, x2, y2, x3, y3, x4, y4);
                    if(temp != null){
                        collisions[counter] = new CollisionSorting(temp.x, temp.y, i, 'b');
                        counter++;
                    }
                }
            }
            for (int i = 0; i < lockedPassage.length; i++) {
                if(lockedPassage[i] != null){
                    x3 = lockedPassage[i].x;
                    y3 = lockedPassage[i].y;
                    x4 = lockedPassage[i].x + lockedPassage[i].width;
                    y4 = lockedPassage[i].y + lockedPassage[i].height;

                    temp = subBulletCollision(x1, y1, x2, y2, x3, y3, x4, y4);
                    if(temp != null){
                        collisions[counter] = new CollisionSorting(temp.x, temp.y, i, 'b');
                        counter++;
                    }
                }
            }
            for(int i = 13; i < 17; i++){ //Clear the boundaries that mark the border of the screen
                mapBorder[i] = null;
            }
            for (int i = 0; i < enemy.length; i++) {
                if(enemy[i] != null){
                    x3 = (int)enemy[i].x;
                    y3 = (int)enemy[i].y;
                    x4 = (int)enemy[i].x + 20;
                    y4 = (int)enemy[i].y + 20;

                    temp = subBulletCollision(x1, y1, x2, y2, x3, y3, x4, y4);
                    if(temp != null){
                        collisions[counter] = new CollisionSorting(temp.x, temp.y, i, 'e');
                        enemyCollision = temp;
                        enemyID = i;
                        counter++;
                    }
                }
            }
            
            if(counter == 0){//check collision of the boundaries of the screen 
                
            }
            
            int closestDist = 700, tempDist;
            CollisionSorting closestPoint, closestPoints[];
            closestPoint = null;
            int outerCounter = counter;
            int id = 0;
            
            closestPoints = new CollisionSorting[counter];
            
            //In the future, I will probably want to sort the whole array, for implemntation of peircing bullets
            for (int i = 0; i < outerCounter; i++) {
                closestPoint = null;
                closestDist = 600;
                for (int j = 0; j < counter; j++) { //counter should be able to keep track of the size of the array
                    if(collisions[j] != null){
                        tempDist = (int)(Math.abs(x1 - collisions[j].x) + Math.abs(y1 - collisions[j].y));
                        if(tempDist < closestDist){
                            closestDist = tempDist;
                            closestPoint = collisions[j];
                            id = j;
                        }
                    }
                }
                closestPoints[i] = closestPoint;
                collisions[id] = null;
            }
            
            //bulletCollision = new Point(0,0);
            //Collision with the borders of the screen not implemented, is implemented above
            //Another map boundary will be built in the mean time to continue other development of the game
            
            //if(closestPoint != null)
            bulletCollision = new Point(0,0);
            if(closestPoints[0] == null){
                System.out.println("Execption: closestPoint == null in bulletCollision()");
                closestPoints[0] = new CollisionSorting((int)player.getX(), (int)player.getY(), 0, 'z');
            }
            
            //Instead, the leftover piercing will be a multiplier for the damage of the last pierce
            //Left over decimal piercing will be handled by using it as a multiplier for damage: damage = damage * piercing(0.1)
            for (int i = 0; i < player.currGun.piercing; i++) { //Rollover peircing
                if(closestPoints[i] != null && closestPoints[i].type == 'e'){
                    int location = closestPoints[i].location;
                    if(i < player.currGun.piercing && i + 1 > player.currGun.piercing){
                        enemy[location].health -= player.currGun.damage * (player.currGun.piercing - i);
                    }else{
                        enemy[location].health -= player.currGun.damage;
                    }
                    
                    
                    enemy[location].timeToMove = (long)(System.currentTimeMillis() + enemy[location].stunCoolDown);
                    if(enemy[location].health <= 0){
                        enemy[location] = null;
                        player.kills++;
                        player.money += 10;
                        if(player.kills % 2 == 0){
                            enemyDifficulty++;
                        }
                    }
                    bulletCollision.x = closestPoints[i].x;
                    bulletCollision.y = closestPoints[i].y;
                }else if(closestPoints[i].type == 'b'){
                    bulletCollision.x = closestPoints[i].x;
                    bulletCollision.y = closestPoints[i].y;
                    break;
                }
            }
            
            //bulletCollision.x = closestPoints[0].x;
            //bulletCollision.y = closestPoints[0].y;
            bullet.x = bulletCollision.x;
            bullet.y = bulletCollision.y;
            
        }
    }
    public void saveToFile(){
        String name = "Save File.txt";
        //System.out.println("What would you like to name this file?");
        //name = input.nextLine();
        //name += ".txt";
        saveFile(name);
        
    }
    private void saveFile(String name){
        char letter;
        File file = new File(name); //File name
        FileWriter out;
        BufferedWriter writeFile;
        String lineOfText;
        
        //Opens file and interacts with it
        try{
            out = new FileWriter(file);
            writeFile = new BufferedWriter(out);
            
            String wordsP[] = player.toString().split(" ");
            
            for (String word: wordsP) {
                writeFile.write(word);
                writeFile.newLine();
            }
            
            writeFile.write("Enemies");
            writeFile.newLine();
            String wordsE[];
            for (int i = 0; i < enemy.length; i++) {
                wordsE = enemy[i].toString().split(" ");
                for (String word: wordsE) {
                    writeFile.write(word);
                    writeFile.newLine();
                }
            }
            
            writeFile.flush();
            writeFile.close();
            System.out.println("Game has been saved."); //Notifies user
        //If there is an error with accessing the file, the user will be alerted
        }catch(FileNotFoundException e){
            System.out.println("File could not be opened.");
            System.out.println("FileNotFoundException:" + e.getMessage());
        }catch(IOException e){
            System.out.println("Problem reading file.");
            System.out.println("IOException:" + e.getMessage());
        }
    }
    private void openFile(String name){
        File file = new File(name); //File name
        FileReader in;
        BufferedReader readFile;
        String lineOfText;
        
        //Opens file and interacts with it
        try{
            in = new FileReader(file);
            readFile = new BufferedReader(in);
            //Reading from file
            while((lineOfText = readFile.readLine()) != null){
                //Reads one character at a time and converts it to an integer
                player.x = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.y = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.health = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.maxHealth = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.speed = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.money = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.kills = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.speedPerk = Integer.parseInt(lineOfText);
                lineOfText = readFile.readLine();
                player.regenPerk = Integer.parseInt(lineOfText);
                lineOfText = readFile.readLine();
                player.healthPerk = Integer.parseInt(lineOfText);
                lineOfText = readFile.readLine();
                player.gun1.damage = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                player.gun1.clipSize = Integer.parseInt(lineOfText);
                lineOfText = readFile.readLine();
                player.gun1.reloadTime = Integer.parseInt(lineOfText);
                lineOfText = readFile.readLine();
                player.gun1.fireRate = Integer.parseInt(lineOfText);
                lineOfText = readFile.readLine();
                player.gun1.piercing = Double.parseDouble(lineOfText);
                lineOfText = readFile.readLine();
                System.out.println("");
                if(!("Enemies".equals(lineOfText))){
                    player.gun2 = new Gun(0,0,0,0,0);
                    player.gun2.damage = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                    player.gun2.clipSize = Integer.parseInt(lineOfText);
                    lineOfText = readFile.readLine();
                    player.gun2.reloadTime = Integer.parseInt(lineOfText);
                    lineOfText = readFile.readLine();
                    player.gun2.fireRate = Integer.parseInt(lineOfText);
                    lineOfText = readFile.readLine();
                    player.gun2.piercing = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                }
                
                for (int i = 0; i < enemy.length; i++) {
                    lineOfText = readFile.readLine();
                    enemy[i].x = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].y = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].health = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].speed = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].damage = Double.parseDouble(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].r = Integer.parseInt(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].b = Integer.parseInt(lineOfText);
                    lineOfText = readFile.readLine();
                    enemy[i].g = Integer.parseInt(lineOfText);
                }
            }
            
        //If there is an error with accessing the file, the user will be alerted
            System.out.println("File has been opened.");
        }catch(FileNotFoundException e){
            System.out.println("File could not be opened.");
            System.out.println("FileNotFoundException:" + e.getMessage());
        }catch(IOException e){
            System.out.println("Problem reading file.");
            System.out.println("IOException:" + e.getMessage());
        }
    }
}

class Structure{
    int x, y, width, height;
    Structure(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
class Line{
    double x1, y1, x2, y2;
    Line(double x1, double y1, double x2, double y2){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
class CollisionSorting{
    int x, y, location;
    char type;
    CollisionSorting(int x, int y, int location, char type){
        this.x = x;
        this.y = y;
        this.location = location; //location is location in the original array, i.e. enemy array or map border array
        this.type = type;
    }
}

public class GPKTowerDefense {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Window wnd = new Window(new Game());
    }
}

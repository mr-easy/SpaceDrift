package com.easystudios.spacedrift;

import android.graphics.PointF;

import java.util.Random;

/**
 * Created by Rishabh on 11-09-2016.
 */
public class Star {

    private PointF position;

    private boolean isVisible = true;

    Random random;

    public Star(int mapWidth, int mapHeight){
        random = new Random();
        position = new PointF();
        position.x = random.nextInt(mapWidth);
        position.y = random.nextInt(mapHeight);
    }

    public void update(){
        //Randomly twinkle the star
        int n = random.nextInt(100);
        if(n == 0){
            //Switch on or off
            isVisible = !isVisible;
        }
    }

    public float getX(){
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public PointF getPosition() {
        return position;
    }

    public boolean getVisibility(){
        return isVisible;
    }
}

package com.easystudios.spacedrift;

import android.graphics.PointF;

import java.util.Random;

/**
 * Created by Rishabh on 11-09-2016.
 */
public class Asteroid {

    Random random;

    private PointF position;
    private float directionAngle;
    private int speed;
    private float horizontalVelocity, verticalVelocity;
    private int size;
    private int color;
    private float worldSizeX, worldSizeY;

    public static final int MAX_SPEED = 200;
    public static final int MIN_SPEED = 50;
    public static final int MAX_RADIUS = 60;
    public static final int MIN_RADIUS = 15;

    public Asteroid(float x, float y, float angle, float worldSizeX, float worldSizeY){
        position = new PointF(x, y);
        directionAngle = angle;
        random = new Random();
        speed = random.nextInt(MAX_SPEED - MIN_SPEED) + MIN_SPEED;
        size = random.nextInt(MAX_RADIUS - MIN_RADIUS) + MIN_RADIUS;

        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;

        horizontalVelocity = (float)(Math.cos(Math.toRadians(angle)));
        verticalVelocity = (float)(Math.sin(Math.toRadians(angle)));
    }

    public  void update(long fps){
        position.x += horizontalVelocity * speed / fps;
        position.y += verticalVelocity * speed / fps;

        if (position.x > worldSizeX){
            position.x -= worldSizeX;
        } else if (position.x < 0){
            position.x += worldSizeX;
        }
        if (position.y > worldSizeY){
            position.y -= worldSizeY;
        } else if (position.y < 0){
            position.y += worldSizeY;
        }
    }

    public PointF getPosition(){
        return position;
    }

    public int getSize(){
        return size;
    }

    public float getDirectionAngle(){
        return directionAngle;
    }

    public void setSize(int size){
        this.size = size;
    }

    public int getSpeed(){
        return speed;
    }

    public void resetPosition(PointF p){
        position = p;
    }

    public void setColor(int Color){
        color = Color;
    }

    public int getColor(){
        return color;
    }
}


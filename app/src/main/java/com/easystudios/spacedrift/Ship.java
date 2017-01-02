package com.easystudios.spacedrift;

import android.content.Context;
import android.graphics.PointF;

/**
 * Created by Rishabh on 11-09-2016.
 */
public class Ship {

    PointF a, b, c, centre;
    float worldSizeX, worldSizeY;
    float facingAngle = 270;
    private float length, width;
    private float speed = 200;
    private float horizontalVelocity, verticalVelocity;
    private float rotationSpeed = 100;

    public static final int FORWARD = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private int shipState = FORWARD;

    public Ship(Context context, float screenX, float screenY, float worldSizeX, float worldSizeY) {
        length = screenX / 10;
        width = screenY / 20;

        a = new PointF();
        b = new PointF();
        c = new PointF();
        centre = new PointF();

        centre.x = screenX / 2;
        centre.y = screenY / 2;

        a.x = centre.x;
        a.y = centre.y - (length * 2 / 3);

        b.x = centre.x - width / 2;
        b.y = centre.y + length / 3;

        c.x = centre.x + width / 2;
        c.y = centre.y + length / 3;

        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;
    }

    public void update(long fps){

        float previousFA = this.facingAngle;

        switch (shipState){
            case RIGHT:
                facingAngle = facingAngle + rotationSpeed / fps;
                if(facingAngle > 360){
                    facingAngle = 1;
                }
                break;
            case LEFT:
                facingAngle = facingAngle - rotationSpeed / fps;
                if(facingAngle < 1){
                    facingAngle = 360;
                }
                break;
            default:
                break;
        }

        /*
        Now rotate each of the three points by
        the change in rotation this frame
        facingAngle - previousFA
        */

        if(shipState != FORWARD) {
            float tempX = 0;
            float tempY = 0;

            // rotate point a
            a.x = a.x - centre.x;
            a.y = a.y - centre.y;

            tempX = (float) (a.x * Math.cos(Math.toRadians(facingAngle - previousFA)) -
                    a.y * Math.sin(Math.toRadians(facingAngle - previousFA)));

            tempY = (float) (a.x * Math.sin(Math.toRadians(facingAngle - previousFA)) +
                    a.y * Math.cos(Math.toRadians(facingAngle - previousFA)));

            a.x = tempX + centre.x;
            a.y = tempY + centre.y;

            // rotate point b
            b.x = b.x - centre.x;
            b.y = b.y - centre.y;

            tempX = (float) (b.x * Math.cos(Math.toRadians(facingAngle - previousFA)) -
                    b.y * Math.sin(Math.toRadians(facingAngle - previousFA)));

            tempY = (float) (b.x * Math.sin(Math.toRadians(facingAngle - previousFA)) +
                    b.y * Math.cos(Math.toRadians(facingAngle - previousFA)));

            b.x = tempX + centre.x;
            b.y = tempY + centre.y;

            // rotate point c
            c.x = c.x - centre.x;
            c.y = c.y - centre.y;

            tempX = (float) (c.x * Math.cos(Math.toRadians(facingAngle - previousFA)) -
                    c.y * Math.sin(Math.toRadians(facingAngle - previousFA)));

            tempY = (float) (c.x * Math.sin(Math.toRadians(facingAngle - previousFA)) +
                    c.y * Math.cos(Math.toRadians(facingAngle - previousFA)));

            c.x = tempX + centre.x;
            c.y = tempY + centre.y;
        }

        //THRUSTING - always
        horizontalVelocity = (float)(Math.cos(Math.toRadians(facingAngle)));
        verticalVelocity = (float)(Math.sin(Math.toRadians(facingAngle)));

        centre.x = centre.x + horizontalVelocity * speed / fps;
        centre.y = centre.y + verticalVelocity * speed / fps;
        if (centre.x > worldSizeX){
            centre.x -= worldSizeX;
            a.x -= worldSizeX;
            b.x -= worldSizeX;
            c.x -= worldSizeX;
        }
        if (centre.x < 0){
            centre.x += worldSizeX;
            a.x += worldSizeX;
            b.x += worldSizeX;
            c.x += worldSizeX;
        }
        if (centre.y > worldSizeY){
            centre.y -= worldSizeY;
            a.y -= worldSizeY;
            b.y -= worldSizeY;
            c.y -= worldSizeY;
        }
        if (centre.y < 0){
            centre.y += worldSizeY;
            a.y += worldSizeY;
            b.y += worldSizeY;
            c.y += worldSizeY;
        }

        a.x = a.x + horizontalVelocity * speed / fps;
        a.y = a.y + verticalVelocity * speed / fps;

        b.x = b.x + horizontalVelocity * speed / fps;
        b.y = b.y + verticalVelocity * speed / fps;

        c.x = c.x + horizontalVelocity * speed / fps;
        c.y = c.y + verticalVelocity * speed / fps;

    }

    public void toggleRotationState()
    {
        if(shipState == Ship.LEFT) {
            shipState = Ship.RIGHT;
        } else {
            shipState = Ship.LEFT;
        }
    }

    /*
    This method will be used to change/set if the
    ship is rotating left or right
    */

    public void setRotationState(int state){
        shipState = state;
    }

    public int getShipState() { return shipState; }

    public PointF getCentre(){
        return  centre;
    }

    public PointF getA(){
        return  a;
    }

    public PointF getB(){
        return  b;
    }

    public PointF getC(){
        return  c;
    }

    float getFacingAngle(){
        return facingAngle;
    }
}

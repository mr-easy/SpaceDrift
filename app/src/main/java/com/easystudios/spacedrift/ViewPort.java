package com.easystudios.spacedrift;

import android.graphics.PointF;

import java.util.Random;

/**
 * Created by Rishabh on 11-09-2016.
 */
public class ViewPort {

    private final float SPEED_MULTIPLIER = 4f;

    private float worldSizeX, worldSizeY, screenResolutionX, screenResolutionY, speedX, speedY;
    private PointF viewportCenter, targetPosition;
    private boolean outside;
    private boolean left, right, up, down;

    public ViewPort(float screenResolutionX, float screenResolutionY, float worldSizeX, float worldSizeY){

        this.screenResolutionX = screenResolutionX;
        this.screenResolutionY = screenResolutionY;
        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;

        viewportCenter = new PointF();
        targetPosition = new PointF();
        viewportCenter.x = screenResolutionX / 2;
        viewportCenter.y = screenResolutionY / 2;

        outside = left = right = up = down = false;
    }

    public void update(long fps){
        speedX = SPEED_MULTIPLIER * (targetPosition.x - viewportCenter.x);
        speedY = SPEED_MULTIPLIER * (targetPosition.y - viewportCenter.y);
        viewportCenter.x += speedX / fps;
        viewportCenter.y += speedY / fps;

        if (viewportCenter.x > worldSizeX){
            viewportCenter.x -= worldSizeX;
        } else if (viewportCenter.x < 0){
            viewportCenter.x += worldSizeX;
        }
        if (viewportCenter.y > worldSizeY){
            viewportCenter.y -= worldSizeY;
        } else if (viewportCenter.y < 0){
            viewportCenter.y += worldSizeY;
        }
    }

    public void setTargetPosition(PointF targetPosition) {
        this.targetPosition = targetPosition;
    }

    public void updateCenter(float x, float y){
        viewportCenter.x = x;
        viewportCenter.y = y;
    }
    public void updateCenter(PointF newCenter){
        viewportCenter.x = newCenter.x;
        viewportCenter.y = newCenter.y;
        left = viewportCenter.x - screenResolutionX / 2 < 0;
        right = viewportCenter.x + screenResolutionX / 2 > worldSizeX;
        up = viewportCenter.y - screenResolutionY / 2 < 0;
        down = viewportCenter.y + screenResolutionY / 2 > worldSizeY;
        outside = left || right || up || down;
    }

    public PointF getPointOutsideView(){
        Random random = new Random();
        float x, y;
        do {
            x = random.nextInt((int) worldSizeX);
        }while(horizontallyInside(x));
        do {
            y = random.nextInt((int) worldSizeY);
        }while (verticallyInside(y));
        return new PointF(x,y);
    }

    public boolean objectInsideViewPort(float objectPosX, float objectPosY, float objectWidth, float objectHeight){

        if (objectPosX - objectWidth/2 <= viewportCenter.x + screenResolutionX/2){
            if (objectPosX + objectWidth/2 >= viewportCenter.x - screenResolutionX/2){
                //horizontallyInside = true;
                if (objectPosY - objectHeight/2 <= viewportCenter.y + screenResolutionY/2){
                    if (objectPosY + objectHeight/2 >= viewportCenter.y - screenResolutionY/2){
                        //verticallyInside = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean objectInsideViewPortIndirectly(float objectPosX, float objectPosY, float objectWidth, float objectHeight){
        if(!outside)
            return false;

        boolean horizontallyInside = true, verticallyInside = true;
        if (left){
            if(!inRange(objectPosX, worldSizeX - (screenResolutionX/2 - viewportCenter.x)/*worldX - extra*/, worldSizeX)){
                horizontallyInside = false;
            }
        } else if (right){
            if (!inRange(objectPosX, 0, viewportCenter.x + screenResolutionX/2 - worldSizeX)){
                horizontallyInside = false;
            }
        }
        if (up){
            if (!inRange(objectPosY, worldSizeY - (/*extra*/screenResolutionY/2 - viewportCenter.y), worldSizeY)){
                verticallyInside= false;
            }
        } else if (down){
            if (!inRange(objectPosY, 0, viewportCenter.y + screenResolutionY/2 - worldSizeY)){
                verticallyInside= false;
            }
        }
        return (horizontallyInside || verticallyInside);
    }

    private boolean horizontallyInside(float x){
        if (x <= viewportCenter.x + screenResolutionX/2)
            if (x >= viewportCenter.x - screenResolutionX/2)
                return true;
        return false;
    }
    private boolean verticallyInside(float y){
        if (y <= viewportCenter.y + screenResolutionY/2)
            if (y >= viewportCenter.y - screenResolutionY/2)
                return true;
        return false;
    }

    public PointF getTransformedPoint(PointF worldPoint){

        //top left point
        float x = viewportCenter.x - screenResolutionX / 2;
        float y = viewportCenter.y - screenResolutionY / 2;

        x = worldPoint.x - x;
        y = worldPoint.y - y;

        return new PointF(x, y);
    }

    public PointF getIndirectTransformedPoint(PointF worldPoint){
        PointF p = new PointF(worldPoint.x, worldPoint.y);
        if (left){
            if(inRange(worldPoint.x, worldSizeX - (screenResolutionX/2 - viewportCenter.x)/*worldX - extra*/, worldSizeX)){
                p.x = worldPoint.x - worldSizeX;
            }
        } else if (right){
            if (inRange(worldPoint.x, 0, viewportCenter.x + screenResolutionX/2 - worldSizeX)){
                p.x = worldPoint.x + worldSizeX;
            }
        }
        if (up){
            if (inRange(worldPoint.y, worldSizeY - (/*extra*/screenResolutionY/2 - viewportCenter.y), worldSizeY)){
                p.y = worldPoint.y - worldSizeY;
            }
        } else if (down){
            if (inRange(worldPoint.y, 0, viewportCenter.y + screenResolutionY/2 - worldSizeY)){
                p.y = worldPoint.y +worldSizeY;
            }
        }

        return getTransformedPoint(p);
    }

    boolean inRange(float value, float min, float max){
        return (value >= Math.min(min, max)) && (value <= Math.max(min, max));
    }

    public boolean isOutside(){
        return outside;
    }

}

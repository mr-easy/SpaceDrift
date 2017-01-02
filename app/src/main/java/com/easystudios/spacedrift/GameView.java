package com.easystudios.spacedrift;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Rishabh on 11-09-2016.
 */
public class GameView extends SurfaceView implements Runnable {

    Context context;
    Thread gameThread = null;
    SurfaceHolder holder;
    volatile boolean playing;
    boolean paused = true;

    private float score;
    private float health;

    Random random = new Random();

    //Drawing
    Canvas canvas;
    Paint paint;

    //frame rate
    long fps;
    float avgFps;
    long frameCount = 0;
    long fpsSum = 0;
    private long timeThisFrame;

    //for collision detection
    boolean collided;

    //resolution
    float screenX, screenY;
    float worldSizeX, worldSizeY;

    //input buttons and joysticks
    int leftBtnX, leftBtnY, rightBtnX, rightBtnY, btnRadius;
    float healthBarLeft, healthBarTop, healthBarRight, healthBarBottom, healthBarWidth;
    boolean left, right;

    //viewport
    ViewPort viewPort;

    //GameObjects
    Ship playerShip;
    Asteroid[] asteroids;
    final int maxAsteroids = 200;
    int asteroidCount = 0;
    //Twinkling stars in the sky
    private Star[] stars = new Star[5000];
    private int numStars;

    //For sound FX
    SoundPool soundPool;
    int thrustID = -1, explodeID = -1, turnID = -1, collisionRubID = -1;
    int thrustStreamID = -1, rubStreamID = -1;

    //Some points to use in draw
    PointF point1, point2, point3, pointC;

    public GameView(final Context context, int screenX, int screenY) {
        super(context);
        this.context = context;
        this.screenX = screenX;
        this.screenY = screenY;
        worldSizeX = screenX * 5;
        worldSizeY = screenY * 5;
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);

        prepareLevel();
    }

    public void prepareLevel(){

        btnRadius = (int) (screenY / 8);
        leftBtnY = rightBtnY = (int) (screenY - btnRadius - 10);
        leftBtnX = btnRadius + 10;
        rightBtnX = (int) (screenX - btnRadius - 10);
        healthBarLeft = screenX/4;
        healthBarRight = 3 * screenX / 4;
        healthBarTop = 20;
        healthBarBottom = 40;
        healthBarWidth = healthBarRight - healthBarLeft;

        playerShip = new Ship(context, screenX, screenY, worldSizeX, worldSizeY);
        asteroids = new Asteroid[maxAsteroids];
        asteroidCount = 0;

        score = 0.0f;
        health = 100.0f;

        viewPort = new ViewPort(screenX, screenY, worldSizeX, worldSizeY);

        point1 = new PointF();
        point2 = new PointF();
        point3 = new PointF();
        pointC = new PointF();

        //prepare Asteroids
        Asteroid asteroid;
        AsteroidDirection ad;
        while (asteroidCount < maxAsteroids){
            ad =  getRandomPositionForAsteroid();
            asteroid = new Asteroid(ad.position.x, ad.position.y, ad.directionAngle, worldSizeX, worldSizeY);
            asteroids[asteroidCount] = asteroid;
            asteroidCount++;
        }

        //Instantiate some stars
        for(int i = 0; i < 5000; i++){
            stars[i] = new Star((int) worldSizeX, (int) worldSizeY);
            numStars++;
        }

        //Load sounds
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(5).build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(status == 0){
                    //thrustStreamID = soundPool.play(thrustID, 1, 1, 1, -1, 1);
                    //soundPool.setVolume(thrustStreamID, 1, 1);
                    rubStreamID = soundPool.play(collisionRubID, 1, 1, 1, -1, 1);
                    soundPool.setLoop(rubStreamID, -1);
                    soundPool.setVolume(rubStreamID, 0, 0);
                }
            }
        });

        try{
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("thrust.wav");
            thrustID = soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd("turn.wav");
            turnID = soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd("explode.wav");
            explodeID = soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd("rub.wav");
            collisionRubID = soundPool.load(descriptor, 1);

        } catch (IOException e) {
            Toast.makeText(context, "error : Failed to load sound files", Toast.LENGTH_SHORT).show();
        }
    }

    public class AsteroidDirection{
        public PointF position;
        public float directionAngle;
        public AsteroidDirection(PointF pos, float angle){
            position = pos;
            directionAngle = angle;
        }
    }

    @Override
    public void run() {

        while(playing){
            long startFrameTime = System.currentTimeMillis();
            if(!paused){
                update();
            }
            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if(timeThisFrame > 1){
                fps = 1000 / timeThisFrame;
                frameCount++;
                fpsSum += fps;
                avgFps = fpsSum / frameCount;
                if(frameCount > 1000) {
                    frameCount = 0;
                    fpsSum = 0;
                }
            }
        }

    }

    private void update(){

        playerShip.update(fps);
        score += (2 / avgFps);

        //update the viewport (after updating the player's position)
        //viewPort.setTargetPosition(playerShip.getCentre()); Change back it to interpolation method
        viewPort.updateCenter(playerShip.getCentre());

        //Update the asteroids
        for (int i = 0; i < asteroidCount; i++) {
            asteroids[i].update(fps);
        }

        //Check for collisions
        //Asteroids and player ship
        //Separating Axis Theorem - SAT
        collided = false;
        for(int i = 0; i < asteroidCount; i++){
            Asteroid o = asteroids[i];
            o.setColor(Color.WHITE);
            PointF r = o.getPosition();
            if(viewPort.objectInsideViewPort(r.x, r.y, o.getSize()*2, o.getSize()*2)){
                //Checking only if the asteroid is inside viewport
                if (!gapInProjectionsOnCANormal(o)){
                    //May be Collision
                    if (!gapInProjectionsOnBCNormal(o)){
                        //May be collision
                        if (!gapInProjectionsOnABNormal(o)){
                            //Confirmed collision
                            collided = true;
                            o.setColor(Color.RED);
                        }
                    }
                }
            }
        }
        if (collided){
            health -= (30 / avgFps);
            soundPool.setVolume(rubStreamID, 1, 1);
        } else {
            health += (0.5 / avgFps);
            soundPool.setVolume(rubStreamID, 0, 0);
        }
        if(health > 100)
            health = 100;
        else if (health < 0) {
            health = 0;
            soundPool.play(explodeID, 1, 1, 1, 0, 1);
            gameOver();
        }

        //Update all stars
        for (int i = 0; i < numStars; i++) {
            stars[i].update();
        }
    }

    private void draw(){

        if(holder.getSurface().isValid()){
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            paint.setColor(Color.argb(255, 255, 255, 255));

            boolean viewportOutside = viewPort.isOutside();

            // Draw stars if visible
            paint.setColor(Color.argb(255, 255, 255, 255));
            if(viewportOutside) {
                for (int i = 0; i < numStars; i++) {
                    if (stars[i].getVisibility()) {
                        if (viewPort.objectInsideViewPort(stars[i].getX(), stars[i].getY(), 2, 2)) {
                            point1 = viewPort.getTransformedPoint(stars[i].getPosition());
                        } else if (viewPort.objectInsideViewPortIndirectly(stars[i].getX(), stars[i].getY(), 2, 2)) {
                            point1 = viewPort.getIndirectTransformedPoint(stars[i].getPosition());
                        }
                        canvas.drawPoint(point1.x, point1.y, paint);
                    }
                }
            } else {
                for (int i = 0; i < numStars; i++) {
                    if (stars[i].getVisibility()) {
                        if (viewPort.objectInsideViewPort(stars[i].getX(), stars[i].getY(), 2, 2)) {
                            point1 = viewPort.getTransformedPoint(stars[i].getPosition());
                            canvas.drawPoint(point1.x, point1.y, paint);
                        }
                    }
                }
            }

            //draw the playerShip
            point1 = viewPort.getTransformedPoint(playerShip.getA());
            point2 = viewPort.getTransformedPoint(playerShip.getB());
            point3 = viewPort.getTransformedPoint(playerShip.getC());
            pointC = viewPort.getTransformedPoint(playerShip.getCentre());
            // Line from a to b
            canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint);
            //b to centre
            canvas.drawLine(point2.x, point2.y, pointC.x, pointC.y, paint);
            //centre to c
            canvas.drawLine(pointC.x, pointC.y, point3.x, point3.y, paint);
            //c to a
            canvas.drawLine(point3.x, point3.y, point1.x, point1.y, paint);
            //centre to a
            canvas.drawLine(pointC.x, pointC.y, point1.x, point1.y, paint);

            paint.setStyle(Paint.Style.STROKE);
            //draw asteroids
            if (viewportOutside) {
                for (int i = 0; i < asteroidCount; i++) {
                    if (viewPort.objectInsideViewPort(asteroids[i].getPosition().x, asteroids[i].getPosition().y, asteroids[i].getSize() * 2, asteroids[i].getSize() * 2)) {
                        paint.setColor(asteroids[i].getColor());
                        point1 = viewPort.getTransformedPoint(asteroids[i].getPosition());
                        canvas.drawCircle(point1.x, point1.y, asteroids[i].getSize(), paint);
                    } else if (viewPort.objectInsideViewPortIndirectly(asteroids[i].getPosition().x, asteroids[i].getPosition().y, asteroids[i].getSize() * 2, asteroids[i].getSize() * 2)) {
                        paint.setColor(asteroids[i].getColor());
                        point1 = viewPort.getIndirectTransformedPoint(asteroids[i].getPosition());
                        canvas.drawCircle(point1.x, point1.y, asteroids[i].getSize(), paint);
                    }
                }
            } else {
                for (int i = 0; i < asteroidCount; i++) {
                    if (viewPort.objectInsideViewPort(asteroids[i].getPosition().x, asteroids[i].getPosition().y, asteroids[i].getSize() * 2, asteroids[i].getSize() * 2)) {
                        paint.setColor(asteroids[i].getColor());
                        point1 = viewPort.getTransformedPoint(asteroids[i].getPosition());
                        canvas.drawCircle(point1.x, point1.y, asteroids[i].getSize(), paint);
                    }
                }
            }

            paint.setColor(Color.WHITE);

            //draw scores and extra
            paint.setTextSize(60);
            canvas.drawText(String.valueOf((int)score), 20, 70, paint);

            //Health Bar
            canvas.drawRect(healthBarLeft, healthBarTop, healthBarRight, healthBarBottom, paint);
            canvas.drawText("FPS = " + avgFps, screenX - 300, 70, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(healthBarLeft, healthBarTop, healthBarLeft + (healthBarWidth * health / 100), healthBarBottom, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void gameOver(){
        paused = true;

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PointF endPoint = playerShip.getCentre();
        Bitmap endScreenshot = getDrawingCache();
        float t1 = System.currentTimeMillis();
        float rad = 1;
        while(System.currentTimeMillis() - t1 < 5000){
            //drawEndFrames();
            canvas.drawBitmap(endScreenshot, 0, 0, paint);
            canvas.drawCircle(endPoint.x, endPoint.y, rad, paint);
            rad += 1/10;
        }

        Intent intent = new Intent(context, MenuActivity.class);
        context.startActivity(intent);
    }
    public void drawEndFrames(){

        if(holder.getSurface().isValid()){
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            paint.setColor(Color.argb(255, 255, 255, 255));

            boolean viewportOutside = viewPort.isOutside();

            // Draw stars if visible
            paint.setColor(Color.argb(255, 255, 255, 255));
            if(viewportOutside) {
                for (int i = 0; i < numStars; i++) {
                    if (stars[i].getVisibility()) {
                        if (viewPort.objectInsideViewPort(stars[i].getX(), stars[i].getY(), 2, 2)) {
                            point1 = viewPort.getTransformedPoint(stars[i].getPosition());
                        } else if (viewPort.objectInsideViewPortIndirectly(stars[i].getX(), stars[i].getY(), 2, 2)) {
                            point1 = viewPort.getIndirectTransformedPoint(stars[i].getPosition());
                        }
                        canvas.drawPoint(point1.x, point1.y, paint);
                    }
                }
            } else {
                for (int i = 0; i < numStars; i++) {
                    if (stars[i].getVisibility()) {
                        if (viewPort.objectInsideViewPort(stars[i].getX(), stars[i].getY(), 2, 2)) {
                            point1 = viewPort.getTransformedPoint(stars[i].getPosition());
                            canvas.drawPoint(point1.x, point1.y, paint);
                        }
                    }
                }
            }

            paint.setStyle(Paint.Style.STROKE);
            //draw asteroids
            if (viewportOutside) {
                for (int i = 0; i < asteroidCount; i++) {
                    if (viewPort.objectInsideViewPort(asteroids[i].getPosition().x, asteroids[i].getPosition().y, asteroids[i].getSize() * 2, asteroids[i].getSize() * 2)) {
                        paint.setColor(asteroids[i].getColor());
                        point1 = viewPort.getTransformedPoint(asteroids[i].getPosition());
                        canvas.drawCircle(point1.x, point1.y, asteroids[i].getSize(), paint);
                    } else if (viewPort.objectInsideViewPortIndirectly(asteroids[i].getPosition().x, asteroids[i].getPosition().y, asteroids[i].getSize() * 2, asteroids[i].getSize() * 2)) {
                        paint.setColor(asteroids[i].getColor());
                        point1 = viewPort.getIndirectTransformedPoint(asteroids[i].getPosition());
                        canvas.drawCircle(point1.x, point1.y, asteroids[i].getSize(), paint);
                    }
                }
            } else {
                for (int i = 0; i < asteroidCount; i++) {
                    if (viewPort.objectInsideViewPort(asteroids[i].getPosition().x, asteroids[i].getPosition().y, asteroids[i].getSize() * 2, asteroids[i].getSize() * 2)) {
                        paint.setColor(asteroids[i].getColor());
                        point1 = viewPort.getTransformedPoint(asteroids[i].getPosition());
                        canvas.drawCircle(point1.x, point1.y, asteroids[i].getSize(), paint);
                    }
                }
            }

            paint.setColor(Color.WHITE);

            //draw scores and extra
            paint.setTextSize(60);
            canvas.drawText(String.valueOf((int)score), 20, 70, paint);

            //Health Bar
            canvas.drawRect(healthBarLeft, healthBarTop, healthBarRight, healthBarBottom, paint);
            canvas.drawText("FPS = " + avgFps, screenX - 300, 70, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(healthBarLeft, healthBarTop, healthBarLeft + (healthBarWidth * health / 100), healthBarBottom, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if(health > 0)
                    paused = false;
                playerShip.toggleRotationState();
                soundPool.play(turnID, 1, 1, 1, 0, 1);
                break;
        }

        return true;
    }

    AsteroidDirection getRandomPositionForAsteroid(){
        float angle = random.nextInt(360) + 1;
        PointF p = viewPort.getPointOutsideView();
        return new AsteroidDirection(p, angle);
    }

    public void pause(){
        playing = false;
        try{
            gameThread.join();
        }catch (InterruptedException e){
            Toast.makeText(context, "ERROR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void resume(){
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    boolean gapInProjectionsOnABNormal(Asteroid o){
        PointF A = playerShip.getA();
        PointF B = playerShip.getB();
        //angle of line AB
        float newAngle = calculateAngle((A.x - B.x), (A.y - B.y)) + 90;
        if (newAngle > 360)
            newAngle -= 360;
        if (newAngle < 0)
            newAngle += 360;
        return collision(newAngle, o);
    }

    boolean gapInProjectionsOnBCNormal(Asteroid o){
        PointF B = playerShip.getB();
        PointF C = playerShip.getC();
        //Angle of line BC
        float newAngle = calculateAngle((B.x - C.x), (B.y - C.y)) + 90;
        if (newAngle > 360)
            newAngle -= 360;
        if (newAngle < 0)
            newAngle += 360;
        return collision(newAngle, o);
    }

    boolean gapInProjectionsOnCANormal(Asteroid o){

        PointF A = playerShip.getA();
        PointF C = playerShip.getC();

        //Angle of line CA
        float newAngle = calculateAngle((A.x - C.x), (A.y - C.y)) + 90;
        if (newAngle > 360)
            newAngle -= 360;
        if (newAngle < 0)
            newAngle += 360;
        return collision(newAngle, o);
    }

    public boolean collision(float newAngle,Asteroid o){

        PointF A = playerShip.getA();
        PointF B = playerShip.getB();
        PointF C = playerShip.getC();
        PointF r;

        //Point A projection
        float mag = (float) Math.sqrt(Math.pow(A.x, 2) + Math.pow(A.y, 2)); //Magnitude of point A
        float angle = calculateAngle(A.x,  A.y); //Angle of point A
        float minPy, maxPy, minAy, maxAy, calc;
        minPy = maxPy = (float) (mag * Math.cos(Math.toRadians(newAngle - angle)));

        //Point B projection
        mag = (float) Math.sqrt(Math.pow(B.x, 2) + Math.pow(B.y, 2)); //Magnitude of point B
        angle = calculateAngle(B.x, B.y); //Angle of point B
        calc = (float) (mag * Math.cos(Math.toRadians(newAngle - angle)));
        if(minPy > calc){
            minPy = calc;
        } else if (maxPy < calc){
            maxPy = calc;
        }

        //Point C projection
        mag = (float) Math.sqrt(Math.pow(C.x, 2) + Math.pow(C.y, 2)); //Magnitude of point C
        angle = calculateAngle(C.x, C.y); //Angle of point C
        calc = (float) (mag * Math.cos(Math.toRadians(newAngle - angle)));
        if(minPy > calc){
            minPy = calc;
        } else if (maxPy < calc){
            maxPy = calc;
        }

        //Projection of Asteroid's centre
        r = o.getPosition();
        mag = (float) Math.sqrt(Math.pow(r.x, 2) + Math.pow(r.y, 2)); //Magnitude of Asteroid's centre point
        angle = calculateAngle(r.x, r.y); //Angle of Asteroid's centre point
        calc = (float) (mag * Math.cos(Math.toRadians(newAngle - angle)));
        minAy = calc - o.getSize();
        maxAy = calc + o.getSize();

        return (minAy > maxPy || minPy > maxAy);
    }

    private float calculateAngle(float x, float y){

        if(x >= 0 && y >= 0)
            return (float) Math.toDegrees(Math.atan(y / x));
        else if(x < 0 && y >= 0)
            return (float) (Math.toDegrees(Math.atan(y / x)) + 180);
        else if(x < 0 && y < 0)
            return (float) (Math.toDegrees(Math.atan(y / x)) + 180);
        else if(x >= 0 && y < 0)
            return (float) (Math.toDegrees(Math.atan(y / x)) + 360);
        return 0;
    }
}

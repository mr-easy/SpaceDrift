package com.easystudios.spacedrift;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    AlertDialog.Builder ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        ad = new AlertDialog.Builder(MenuActivity.this);
        ad.setMessage("Leaving so soon?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        ad.setCancelable(true);
    }



    @Override

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MenuActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void quit(){

        ad.show();
    }


    class MyBounceInterpolator implements android.view.animation.Interpolator {
        double mAmplitude = 1;
        double mFrequency = 10;

        MyBounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }

    public void play(View view) {
        ImageButton button = (ImageButton)findViewById(R.id.button);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 100);
        myAnim.setInterpolator(interpolator);
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        startActivity(intent);

        button.startAnimation(myAnim);
    }

    public void help(View view) {
        ImageButton button = (ImageButton)findViewById(R.id.button2);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 100);
        myAnim.setInterpolator(interpolator);
      //  Intent intent = new Intent(MenuActivity.this, MainActivity.class);
      //  startActivity(intent);
        Toast.makeText(MenuActivity.this, "help", Toast.LENGTH_SHORT).show();
        button.startAnimation(myAnim);
    }
    public void credits(View view) {
        ImageButton button = (ImageButton)findViewById(R.id.button3);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 100);
        myAnim.setInterpolator(interpolator);
        //  Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        //  startActivity(intent);
        Toast.makeText(MenuActivity.this, "Developer", Toast.LENGTH_SHORT).show();
        button.startAnimation(myAnim);
    }
    public void quit(View view) {
        ImageButton button = (ImageButton)findViewById(R.id.button4);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 100);
        myAnim.setInterpolator(interpolator);
         onBackPressed();
        button.startAnimation(myAnim);
    }

}

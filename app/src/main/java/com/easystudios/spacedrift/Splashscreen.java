package com.easystudios.spacedrift;

/**
 * Created by anura on 1/15/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splashscreen extends Activity {
    private static int SPLASH_TIME_OUT = 2000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Splashscreen.this.startActivity(new Intent(Splashscreen.this, MenuActivity.class));
                Splashscreen.this.finish();
            }
        }, (long) SPLASH_TIME_OUT);
    }
}

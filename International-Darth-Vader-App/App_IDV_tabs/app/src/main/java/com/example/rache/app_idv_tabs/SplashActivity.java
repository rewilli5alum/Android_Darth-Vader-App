package com.example.rache.app_idv_tabs;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Timer;

/**
 * This activity will handle the splash page for the app
 */

public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        VideoView videoHolder = (VideoView)findViewById(R.id.videoViewRelative);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.splash_with_music);
        videoHolder.setVideoURI(video);
        videoHolder.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                jump();
            }

        });
        videoHolder.start();
    }

    private void jump() {
        if(isFinishing())
            return;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        jump();
        return true;
    }

}

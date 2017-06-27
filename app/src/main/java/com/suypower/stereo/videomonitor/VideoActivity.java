package com.suypower.stereo.videomonitor;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

public class VideoActivity extends Activity {

    private VideoView videoView;
    private ImageView btncontroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        videoView = (VideoView)findViewById(R.id.v1);
        btncontroller = (ImageView)findViewById(R.id.btncontroller);
        String url = getIntent().getStringExtra("url");
        videoView.setVideoURI(Uri.parse(url));
        videoView.setOnPreparedListener(onPreparedListener);

        btncontroller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    btncontroller.setBackground(getResources().getDrawable(R.mipmap.play_normal));
                }
                else {
                    videoView.resume();
                    btncontroller.setBackground(getResources().getDrawable(R.mipmap.stop_normal));
                }

            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode ==4)
        {
            videoView.pause();

        }
        return super.onKeyUp(keyCode, event);
    }

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setVolume(0f, 0f);
            mp.setLooping(false);
            mp.start();
        }

    };
}

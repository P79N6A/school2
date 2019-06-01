package com.freshtribes.icecream.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.freshtribes.icecream.R;

import org.jetbrains.annotations.Nullable;

/**
 * Created by Administrator on 2018/1/15.
 */

public class AudioService extends Service {

    private static final String TAG = "AudioService";

    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBind();
    }

    public class MyBind extends Binder
    {
        public AudioService getService()
        {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);

        //播放完毕 进行监听回调
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();//释放资源
            }
        });

        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void playBuy(){
        if(mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(AudioService.this, R.raw.mp3buy);
            mediaPlayer.start();
        }
    }

    public void playWelcome(){
        if(mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(AudioService.this, R.raw.mp3welcome);
            mediaPlayer.start();
        }
    }

    public void playSayAgain(){
        if(mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(AudioService.this, R.raw.mp3sayagain);
            mediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.release();

        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

}

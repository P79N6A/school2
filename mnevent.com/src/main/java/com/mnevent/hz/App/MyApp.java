package com.mnevent.hz.App;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.multidex.MultiDexApplication;

import com.lzy.okgo.OkGo;

import org.litepal.LitePal;

/**
 * Created by zyand on 2019/5/18.
 */

public class MyApp extends MultiDexApplication {


    public static MyApp mApplication ;
    public static Typeface fromAsset;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        LitePal.initialize(getApplicationContext());
        AssetManager assets = this.getAssets();
        fromAsset = Typeface.createFromAsset(assets, "fonts/yzqs.ttf");

    }
}

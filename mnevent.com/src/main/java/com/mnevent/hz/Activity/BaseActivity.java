package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mnevent.hz.Utils.Activitmessage;


/**
 * Created by zyand on 2019/3/15.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("活动名：",getClass().getSimpleName());

        Activitmessage.AddActivity(this);


        initInputManager();
    }

    private void initInputManager() {
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * 初始化方法
     */
    protected abstract void init();

   /* *//**
     * 初始化view方法
     *//*
    protected abstract void initview();

    *//**
     * 加载数据
     *//*
    public abstract void initdata();*/

    /**
     * 行
     */
    protected abstract void initclick();


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        // TODO onStart
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Activitmessage.RemoveActivity(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        savedInstanceState = null;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    public int dip2px(float dpValue) {
        final float scale = getResources()
                .getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public int px2dip(float pxValue) {
        final float scale = getResources()
                .getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    protected void showToast(String text, int duration) {
        Toast.makeText(this, text, duration).show();
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param cls    目标activity
     * @param intent 可传空
     * @Time:2017-7-31: 下午6:00:42
     * @author: JinPeng
     * @category 跳转方法
     */
    public void start(Class<?> cls, Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.setClass(this, cls);
        startActivity(intent);
    }

    /**
     * 获取软键盘状态
     */
    public boolean isKeyBoardOpen() {
        if (inputMethodManager == null)
            return false;
        boolean isOpen = inputMethodManager.isActive();// isOpen若返回true，则表示输入法打开
        return isOpen;
    }

    /**
     * 显示软键盘
     */
    public void showKeyBoard() {
        if (inputMethodManager == null)
            return;
        inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);// show-input
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyBoard() {
        if (inputMethodManager == null || !(inputMethodManager.isActive()))
            return;
        try {
            View focusView = getCurrentFocus();
            if (focusView != null) {
                inputMethodManager.hideSoftInputFromWindow(focusView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


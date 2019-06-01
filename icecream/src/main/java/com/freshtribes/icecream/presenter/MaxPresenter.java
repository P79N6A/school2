package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.view.IMaxView;


/**
 * Created by Administrator on 2017/8/22.
 */

public class MaxPresenter {
    private IMaxView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public MaxPresenter(IMaxView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void changeMax(final int position,final int dbtrackno,final int max){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                    db.execSQL("update trackmaingeneral set nummax=" + max + " where id=" + dbtrackno );  // 从10开始的
                    myApp.getTrackMainGeneral()[dbtrackno-10].setNummax(max);  // 从0开始的

                    db.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                iView.changeMax(position,max);

            }
        });

        thread.start();

    }

}


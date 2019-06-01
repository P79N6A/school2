package com.freshtribes.icecream.view;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface IMainView {

    // 出货结果（空表示成功出货）
    void rtnVendOutInd(String[] outresult);

    // 取得状态的结果
    void rtnGetStatus(String temp,String doorstatus);

    // 如果不是Machine ok 或者 不是 Delivery ok,那么在屏幕上Toast显示一下
    void rtnDispStatus(String status);

    // 这2个函数是从mainActivity取数据的
    String getClientID();
    String dispNetStatus();

    void HaveErrorFreshDisp();

    void rtnFaceMoneyCheck(String rtnstr);
}

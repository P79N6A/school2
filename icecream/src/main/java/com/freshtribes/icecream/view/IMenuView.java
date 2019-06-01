package com.freshtribes.icecream.view;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface IMenuView {

    void getVersionError(String msgstr);

    void rtnGetVersionInfo(long advertplanid, long trackplanid, String apkserverversion);
}

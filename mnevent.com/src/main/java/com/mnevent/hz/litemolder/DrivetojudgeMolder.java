package com.mnevent.hz.litemolder;

import org.litepal.crud.LitePalSupport;

/**
 * Created by zyand on 2018/12/30.
 * 驱动判断
 */

public class DrivetojudgeMolder extends LitePalSupport {

    private String drive;//选择的主控驱动
    private String hostnumber;//主机编号
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDrive() {
        return drive;
    }

    public void setDrive(String drive) {
        this.drive = drive;
    }

    public String getHostnumber() {
        return hostnumber;
    }

    public void setHostnumber(String hostnumber) {
        this.hostnumber = hostnumber;
    }
}

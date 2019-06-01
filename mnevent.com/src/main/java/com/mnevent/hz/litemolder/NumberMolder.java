package com.mnevent.hz.litemolder;

import org.litepal.crud.LitePalSupport;

/**
 * Created by zyand on 2018/12/28.
 */

public class NumberMolder extends LitePalSupport {

    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}

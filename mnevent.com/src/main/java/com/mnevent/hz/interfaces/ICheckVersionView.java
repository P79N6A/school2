package com.mnevent.hz.interfaces;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface ICheckVersionView {

    // 保存出错了
    void downloadError(String msgstr);

    // 保存成功
    void downloadOK(String dlurl, String serverversion);
}

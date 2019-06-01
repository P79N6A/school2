package com.freshtribes.icecream.view;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface ILoadingView {

    // 启动数据从数据库和配置文件中读取完成
    void loadingDataOver(String status);

    void dispMessage(String msgstr);
}

package com.freshtribes.icecream.view;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface ITrackPlanView {

    // 取得服务器端的最新货道方案
    void rtnServerTrackPlan(int code, String msg, long trackplanid, String trackplanname,
                            String maintrackflag, String subtrackflag, ArrayList<HashMap<String, Object>> arrayList);

    // 下载图片失败出错了
    void downgoodsinfoerror(String msgstr);

    // 下载完成
    void downgoodsinfook();

    // 下载中的信息
    void downgoodsinfodoing(String msgstr);

    // 对应的商品名称
    void setgoodsname(String goodscode, String goodsname);

}

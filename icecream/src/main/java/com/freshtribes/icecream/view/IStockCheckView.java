package com.freshtribes.icecream.view;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface IStockCheckView {

    // 保存出错了
    void saveError(String msgstr);

    // 保存成功
    void saveOK();
}

package com.freshtribes.icecream.view;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface ISaleCountView {

    void rtnSaleCount(long totalsum, long totalcount, long cashsum, long cashcount,
                      long alisum, long alicount, long wxsum, long wxcount, long facesum, long facecount, long membersum, long membercount);

    void rtnSaleCountError(String msgstr);

}

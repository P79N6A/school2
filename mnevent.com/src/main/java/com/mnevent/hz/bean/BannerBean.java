package com.mnevent.hz.bean;

import java.util.List;

/**
 * Created by zyand on 2019/1/8.
 */

public class BannerBean {


    /**
     * advertPlanVo : {"adver":[{"adpugurl":"http://lbx.freshtribes.com/adverimageAR/1_1.png"},{"adpugurl":"http://lbx.freshtribes.com/adverimageAR/1_2.png"},{"adpugurl":"http://lbx.freshtribes.com/adverimageAR/1_3.png"}]}
     * code : 2
     * message : 成功
     */

    private AdvertPlanVoBean advertPlanVo;
    private String code;
    private String message;

    public AdvertPlanVoBean getAdvertPlanVo() {
        return advertPlanVo;
    }

    public void setAdvertPlanVo(AdvertPlanVoBean advertPlanVo) {
        this.advertPlanVo = advertPlanVo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class AdvertPlanVoBean {
        private List<AdverBean> adver;

        public List<AdverBean> getAdver() {
            return adver;
        }

        public void setAdver(List<AdverBean> adver) {
            this.adver = adver;
        }

        public static class AdverBean {
            /**
             * adpugurl : http://lbx.freshtribes.com/adverimageAR/1_1.png
             */

            private String adpugurl;

            public String getAdpugurl() {
                return adpugurl;
            }

            public void setAdpugurl(String adpugurl) {
                this.adpugurl = adpugurl;
            }
        }
    }
}

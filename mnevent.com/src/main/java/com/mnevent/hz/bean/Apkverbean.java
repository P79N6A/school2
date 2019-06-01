package com.mnevent.hz.bean;

/**
 * Created by zyand on 2019/1/22.
 */

public class Apkverbean {

    /**
     * apk : {"apkname":"com.mnevent.hz","apkver":100,"comment":"第一个版本","dlurl":"http://app.freshtribes.com/apk/medicine100.apk","updatetime":"2019-01-22 16:00:57.0"}
     * code : 2
     * message : 成功
     */

    private ApkBean apk;
    private String code;
    private String message;

    public ApkBean getApk() {
        return apk;
    }

    public void setApk(ApkBean apk) {
        this.apk = apk;
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

    public static class ApkBean {
        /**
         * apkname : com.mnevent.hz
         * apkver : 100
         * comment : 第一个版本
         * dlurl : http://app.freshtribes.com/apk/medicine100.apk
         * updatetime : 2019-01-22 16:00:57.0
         */

        private String apkname;
        private int apkver;
        private String comment;
        private String dlurl;
        private String updatetime;

        public String getApkname() {
            return apkname;
        }

        public void setApkname(String apkname) {
            this.apkname = apkname;
        }

        public int getApkver() {
            return apkver;
        }

        public void setApkver(int apkver) {
            this.apkver = apkver;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getDlurl() {
            return dlurl;
        }

        public void setDlurl(String dlurl) {
            this.dlurl = dlurl;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }
    }
}

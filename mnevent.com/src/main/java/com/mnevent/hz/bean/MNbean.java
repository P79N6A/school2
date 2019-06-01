package com.mnevent.hz.bean;

/**
 * Created by zyand on 2019/5/28.
 */

public class MNbean {

    /**
     * code : 0000
     * message : success
     * result : {"exist":false,"isReceive":false}
     */

    private String code;
    private String message;
    private ResultBean result;

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

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * exist : false
         * isReceive : false
         */

        private boolean exist;
        private boolean isReceive;

        public boolean isExist() {
            return exist;
        }

        public void setExist(boolean exist) {
            this.exist = exist;
        }

        public boolean isIsReceive() {
            return isReceive;
        }

        public void setIsReceive(boolean isReceive) {
            this.isReceive = isReceive;
        }
    }
}

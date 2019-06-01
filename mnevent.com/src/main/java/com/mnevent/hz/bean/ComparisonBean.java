package com.mnevent.hz.bean;

/**
 * Created by zyand on 2018/12/29.
 * 眼镜对比
 */

public class ComparisonBean {

    private String image;//商品图片

    private int post;

    private String code;
    private String texts;


    public String getTexts() {
        return texts;
    }

    public void setTexts(String texts) {
        this.texts = texts;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

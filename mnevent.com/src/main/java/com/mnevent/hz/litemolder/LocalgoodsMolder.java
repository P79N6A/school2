package com.mnevent.hz.litemolder;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

/**
 * Created by zyand on 2019/5/22.
 */

public class LocalgoodsMolder extends LitePalSupport {
    private int id;
    //商品图片
    private String image;

    //商品选择
    private int pd ;
    //商品类别
    private String type;
    //商品状态
    private String code;
    //商品名称
    private String name;
    //商品价格
    private String price;
    //库存
    private String inventory;
    //是否进入列表
    private String up;//0未放入列表，1放入列表

    //列表的位置
    private String post;
    //商品id
    private String goodscode;

    public String getGoodscode() {
        return goodscode;
    }

    public void setGoodscode(String goodscode) {
        this.goodscode = goodscode;
    }

    public String getUp() {
        return up;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }



    public int getPd() {
        return pd;
    }

    public void setPd(int pd) {
        this.pd = pd;
    }
}

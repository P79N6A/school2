package com.mnevent.hz.litemolder;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Administrator on 2018/12/15.
 * 商品轨道合并后完整的数据
 */

public class ProductMolder extends LitePalSupport {

    private int id;
    //商品图片
    private int image;
    //商品传给sdk的值
    private String texts;
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
    private int inventory;
    //是否进入列表
    private String up = "0";//0未放入列表，1放入列表
    //列表中的位置
    private String post;



   // private PathwayMolder molder;

   /* public PathwayMolder getMolder() {
        //子表中会生成一个关联父表的id供父表查询，且字表中id生成符合规则："父表类名小写_id"
        //若父表为Person类(父表中会自动生成一个id自增列)，子表为User类,则字表中会自动生成字段person_id对应父表中id，以供查询
        String linkId=this.getClass().getSimpleName().toLowerCase();
        List<PathwayMolder> list= LitePal.where(linkId+"_id=?",String.valueOf(id)).find(PathwayMolder.class);
        if(list == null){
            molder= null;
        }else{
            molder=list.get(0);
        }

        return molder;
    }

    public void setMolder(PathwayMolder molder) {
        molder.save();
        this.molder = molder;
    }*/

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getUp() {
        return up;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTexts() {
        return texts;
    }

    public void setTexts(String texts) {
        this.texts = texts;
    }

    public int getPd() {
        return pd;
    }

    public void setPd(int pd) {
        this.pd = pd;
    }
}

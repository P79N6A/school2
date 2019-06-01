package com.mnevent.hz.litemolder;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Administrator on 2018/12/15.、
 * 轨道及商品信息
 */

public class PathwayMolder extends LitePalSupport {

    private int id;
    private String post;
    private String code;//轨道号

    private int nummax ;//最大存放数
    private int numnow ;//现在存放数量

    private String errorcode ;  // 1表示没有故障，0表示有故障

    private String errortime;   //故障发生时间

    private String mergecode ;  //1表示合并后可用的，0表示被禁用的,2表示被合并的

    private String steps ; //表示1次所走的步数

    private int cansale = 1;  // 1可以销售，0表示不能销售（强制停止的意思），这是由后台强制锁定机器的轨道的时候使用，目前暂不开放

    private String pds ; //是否存在，0不存在，1存在

    private int chain;//链条

    //商品id
    private String goodscode;
    //商品图片
    private String image;
    //商品传给sdk的值
    private String texts;
    //商品选择
    private int pd ;
    //商品类别
    private String pretype;
    //商品状态
    private String precode;
    //商品名称
    private String name;
    //商品价格
    private String price;
    //库存
    private String inventory;
    //是否进入列表
    private String up ;//0未放入列表，1放入列表

    private String monetaryunit;//价钱单位

    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMonetaryunit() {
        return monetaryunit;
    }

    public void setMonetaryunit(String monetaryunit) {
        this.monetaryunit = monetaryunit;
    }


/*private ProductlibMolder molder;

    public ProductlibMolder getMolder() {
        //子表中会生成一个关联父表的id供父表查询，且字表中id生成符合规则："父表类名小写_id"
        //若父表为Person类(父表中会自动生成一个id自增列)，子表为User类,则字表中会自动生成字段person_id对应父表中id，以供查询
        String linkId=this.getClass().getSimpleName().toLowerCase();
        List<ProductlibMolder> list= LitePal.where(linkId+"_id=?",String.valueOf(id)).find(ProductlibMolder.class);
        if(list == null){
            molder= null;
        }else{
            molder=list.get(0);
        }

        return molder;
    }

    public void setMolder(ProductlibMolder molder) {
        molder.save();
        this.molder = molder;
    }*/

    public int getCansale() {
        return cansale;
    }

    public void setCansale(int cansale) {
        this.cansale = cansale;
    }

    public String getGoodscode() {
        return goodscode;
    }

    public void setGoodscode(String goodscode) {
        this.goodscode = goodscode;
    }

    public String getErrortime() {
        return errortime;
    }

    public void setErrortime(String errortime) {
        this.errortime = errortime;
    }

    public int getChain() {
        return chain;
    }

    public void setChain(int chain) {
        this.chain = chain;
    }

    public void setPd(int pd) {
        this.pd = pd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPd() {
        return pd;
    }

    public String getPds() {
        return pds;
    }

    public void setPds(String pds) {
        this.pds = pds;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTexts() {
        return texts;
    }

    public void setTexts(String texts) {
        this.texts = texts;
    }

    public String getPretype() {
        return pretype;
    }

    public void setPretype(String pretype) {
        this.pretype = pretype;
    }

    public String getPrecode() {
        return precode;
    }

    public void setPrecode(String precode) {
        this.precode = precode;
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

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
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



    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getNummax() {
        return nummax;
    }

    public void setNummax(int nummax) {
        this.nummax = nummax;
    }

    public int getNumnow() {
        return numnow;
    }

    public void setNumnow(int numnow) {
        this.numnow = numnow;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    public String getMergecode() {
        return mergecode;
    }

    public void setMergecode(String mergecode) {
        this.mergecode = mergecode;
    }
}

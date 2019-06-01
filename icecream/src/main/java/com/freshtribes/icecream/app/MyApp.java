package com.freshtribes.icecream.app;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.baidu.aip.FaceDetector;
import com.baidu.aip.face.turnstile.ResourceSettings;
import com.freshtribes.icecream.baiduai.APIService;
import com.freshtribes.icecream.baiduai.Config;
import com.freshtribes.icecream.bean.TrackBean;


/**
 * Created by Administrator on 2017/8/22.
 */

public class MyApp extends Application {

    //private final boolean isTestMode = true;

    private final String serverurl = "http://mac.freshtribes.com/macservice";

    private String mainMacID = "";
    private String mainMacType = "icecream";//这个是固定的，就是冰淇淋机,或者普通综合机
    private String accessKey = "";

    private int mainGeneralLevelNum;
    private int mainGeneralLevel1TrackCount;
    private int mainGeneralLevel2TrackCount;
    private int mainGeneralLevel3TrackCount;
    private int mainGeneralLevel4TrackCount;
    private int mainGeneralLevel5TrackCount;
    private int mainGeneralLevel6TrackCount;
    private int mainGeneralLevel7TrackCount;

    private TrackBean[] trackMainGeneral = new TrackBean[70];
    // 70个轨道的走步
    private int[] magexStep = new int[]{3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3};
    private int magexallstep = 18;

    private long trackplanid;
    private String trackplanname;

    // 最近一次的盘点时间
    private String stockchecktime="";

    // login时的用户ID，用户登录名，补货单号
    private long tempUserId;
    private String tempUserName;
    private String tempSupplyCode;

    private int haveFacePay = 0;// 缺省是没有的
    // 是否有mic，缺省是没有的
    private int michave = 0;
    private int haveMember = 0;// 会员 缺省是没有的

    private int haveAlipay;// 标准的支付宝
    private int havaWeixin;// 标准的微信

    // 启用银联二维码支付，则不需要支付宝和微信的二维码。不启用，则启用支付宝和微信的二维码
    private int haveUms = 0;// 银联二维码支付，缺省是不启用

    private int haveZXAlipay;// 接入中信银行的支付宝
    private int haveZXWxpay;// 接入中信银行的的微信

    private int devicetype = 0; // 0:中亚冰激凌升降机","1:Magex有主控链板","2:Magex有主控皮带或弹簧"



    // 支付倒计时，因为人脸识别activity也要用，就放到App
    private String pay_count = "";
    public String getPay_count() {
        return pay_count;
    }
    public void setPay_count(String pay_count) {
        this.pay_count = pay_count;
    }






    // -------------------------------------------------------  //
    //public boolean getTestMode() { return isTestMode;}

    public int getMichave() {
        return michave;
    }
    public void setMichave(int michave) {
        this.michave = michave;
    }

    public int getHaveFacePay() {
        return haveFacePay;
    }
    public void setHaveFacePay(int haveFacePay) {
        this.haveFacePay = haveFacePay;
    }

    public int getHaveMember() {
        return haveMember;
    }
    public void setHaveMember(int haveMember) {
        this.haveMember = haveMember;
    }

    public int getHaveUms() {
        return haveUms;
    }
    public void setHaveUms(int haveUms) {
        this.haveUms = haveUms;
    }

    public int getDevicetype() {
        return devicetype;
    }
    public void setDevicetype(int devicetype) {
        this.devicetype = devicetype;
    }

    public int getHaveAlipay() {
        return haveAlipay;
    }

    public void setHaveAlipay(int haveAlipay) {
        this.haveAlipay = haveAlipay;
    }

    public int getHavaWeixin() {
        return havaWeixin;
    }

    public void setHavaWeixin(int havaWeixin) {
        this.havaWeixin = havaWeixin;
    }

    public int getHaveZXAlipay() {
        return haveZXAlipay;
    }

    public void setHaveZXAlipay(int haveZXAlipay) {
        this.haveZXAlipay = haveZXAlipay;
    }

    public int getHaveZXWxpay() {
        return haveZXWxpay;
    }

    public void setHaveZXWxpay(int haveZXWxpay) {
        this.haveZXWxpay = haveZXWxpay;
    }

    public long getTempUserId() {
        return tempUserId;
    }

    public void setTempUserId(long tempUserId) {
        this.tempUserId = tempUserId;
    }

    public String getTempUserName() {
        return tempUserName;
    }

    public void setTempUserName(String tempUserName) {
        this.tempUserName = tempUserName;
    }

    public String getTempSupplyCode() {
        return tempSupplyCode;
    }

    public void setTempSupplyCode(String tempSupplyCode) {
        this.tempSupplyCode = tempSupplyCode;
    }

    public long getTrackplanid() {
        return trackplanid;
    }

    public void setTrackplanid(long trackplanid) {
        this.trackplanid = trackplanid;
    }

    public String getTrackplanname() {
        return trackplanname;
    }

    public void setTrackplanname(String trackplanname) {
        this.trackplanname = trackplanname;
    }

    public String getStockchecktime() {
        return stockchecktime;
    }

    public void setStockchecktime(String stockchecktime) {
        this.stockchecktime = stockchecktime;
    }


    public TrackBean[] getTrackMainGeneral() {
        return trackMainGeneral;
    }

    public void setTrackMainGeneral(TrackBean[] trackMainGeneral) {
        this.trackMainGeneral = trackMainGeneral;
    }


    public String getServerurl() {
        return serverurl;
    }

    public String getMainMacID() {
        return mainMacID;
    }

    public void setMainMacID(String mainMacID) {
        this.mainMacID = mainMacID;
    }

    public String getMainMacType() {
        return mainMacType;
    }

    public void setMainMacType(String mainMacType) {
        this.mainMacType = mainMacType;
    }



    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }


    public int getMainGeneralLevelNum() {
        return mainGeneralLevelNum;
    }

    public void setMainGeneralLevelNum(int mainGeneralLevelNum) {
        this.mainGeneralLevelNum = mainGeneralLevelNum;
    }

    public int getMainGeneralLevel1TrackCount() {
        return mainGeneralLevel1TrackCount;
    }

    public void setMainGeneralLevel1TrackCount(int mainGeneralLevel1TrackCount) {
        this.mainGeneralLevel1TrackCount = mainGeneralLevel1TrackCount;
    }

    public int getMainGeneralLevel2TrackCount() {
        return mainGeneralLevel2TrackCount;
    }

    public void setMainGeneralLevel2TrackCount(int mainGeneralLevel2TrackCount) {
        this.mainGeneralLevel2TrackCount = mainGeneralLevel2TrackCount;
    }

    public int getMainGeneralLevel3TrackCount() {
        return mainGeneralLevel3TrackCount;
    }

    public void setMainGeneralLevel3TrackCount(int mainGeneralLevel3TrackCount) {
        this.mainGeneralLevel3TrackCount = mainGeneralLevel3TrackCount;
    }

    public int getMainGeneralLevel4TrackCount() {
        return mainGeneralLevel4TrackCount;
    }

    public void setMainGeneralLevel4TrackCount(int mainGeneralLevel4TrackCount) {
        this.mainGeneralLevel4TrackCount = mainGeneralLevel4TrackCount;
    }

    public int getMainGeneralLevel5TrackCount() {
        return mainGeneralLevel5TrackCount;
    }

    public void setMainGeneralLevel5TrackCount(int mainGeneralLevel5TrackCount) {
        this.mainGeneralLevel5TrackCount = mainGeneralLevel5TrackCount;
    }

    public int getMainGeneralLevel6TrackCount() {
        return mainGeneralLevel6TrackCount;
    }

    public void setMainGeneralLevel6TrackCount(int mainGeneralLevel6TrackCount) {
        this.mainGeneralLevel6TrackCount = mainGeneralLevel6TrackCount;
    }

    public int getMainGeneralLevel7TrackCount() {
        return mainGeneralLevel7TrackCount;
    }

    public void setMainGeneralLevel7TrackCount(int mainGeneralLevel7TrackCount) {
        this.mainGeneralLevel7TrackCount = mainGeneralLevel7TrackCount;
    }

    public int[] getMagexStep() {
        return magexStep;
    }
    public void setMagexStep(int[] magexStep) {
        this.magexStep = magexStep;
    }
    public int getMagexallstep() {
        return magexallstep;
    }

    public void setMagexallstep(int magexallstep) {
        this.magexallstep = magexallstep;
    }


    private Handler handler = new Handler(Looper.getMainLooper());

    public static final float VALUE_BRIGHTNESS = 40.0F;
    public static final float VALUE_BLURNESS = 0.7F;
    public static final float VALUE_OCCLUSION = 0.6F;
    public static final int VALUE_HEAD_PITCH = 15;
    public static final int VALUE_HEAD_YAW = 15;
    public static final int VALUE_HEAD_ROLL = 15;
    public static final int VALUE_CROP_FACE_SIZE = 400;
    public static final int VALUE_MIN_FACE_SIZE = 120;
    public static final float VALUE_NOT_FACE_THRESHOLD = 0.6F;

    @Override
    public void onCreate() {
        super.onCreate();


        /*
        FaceSDKManager.getInstance().initialize(this, Config.licenseID, Config.licenseFileName);
        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
        // 设置活体动作，通过设置list LivenessTypeEnum.Eye，LivenessTypeEnum.Mouth，LivenessTypeEnum.HeadUp，
        // LivenessTypeEnum.HeadDown，LivenessTypeEnum.HeadLeft, LivenessTypeEnum.HeadRight,
        // LivenessTypeEnum.HeadLeftOrRight
        List<LivenessTypeEnum> livenessList = new ArrayList<>();
        livenessList.add(LivenessTypeEnum.Mouth);
        livenessList.add(LivenessTypeEnum.Eye);
//        livenessList.add(LivenessTypeEnum.HeadUp);
//        livenessList.add(LivenessTypeEnum.HeadDown);
//        livenessList.add(LivenessTypeEnum.HeadLeft);
//        livenessList.add(LivenessTypeEnum.HeadRight);
        config.setLivenessTypeList(livenessList);
        // 设置 活体动作是否随机 boolean
        config.setLivenessRandom(true);
        config.setLivenessRandomCount(1);
        // 模糊度范围 (0-1) 推荐小于0.7
        config.setBlurnessValue(VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        config.setBrightnessValue(VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        config.setCropFaceValue(VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        config.setHeadPitchValue(VALUE_HEAD_PITCH);
        config.setHeadRollValue(VALUE_HEAD_ROLL);
        config.setHeadYawValue(VALUE_HEAD_YAW);
        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        config.setMinFaceSize(VALUE_MIN_FACE_SIZE);
        // 人脸置信度（0-1）推荐大于0.6
        config.setNotFaceValue(VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        config.setOcclusionValue(VALUE_OCCLUSION);
        // 是否进行质量检测
        config.setCheckFaceQuality(true);
        // 人脸检测使用线程数
        config.setFaceDecodeNumberOfThreads(2);
        // 是否开启提示音
        config.setSound(true);

        FaceSDKManager.getInstance().setFaceConfig(config);
        */

        ResourceSettings.init();
        // 初始化人脸库
        FaceDetector.init(this, Config.licenseID, Config.licenseFileName);
        // 设置最小人脸，小于此值的人脸不会被识别
        FaceDetector.getInstance().setMinFaceSize(100);
        FaceDetector.getInstance().setCheckQuality(false);

        // 头部的欧拉角，大于些值的不会被识别
        FaceDetector.getInstance().setEulerAngleThreshold(45, 45, 45);



        APIService.getInstance().init(this);

//        Log.i("icecream","MyApp 人脸FaceSDK:" + FaceSDKManager.isLicenseSuccess());
//        APIService.getInstance().setGroupId(Config.groupID);
//         用ak，sk获取token, 调用在线api，如：注册、识别等。为了ak、sk安全，建议放您的服务器，
//        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
//            @Override
//            public void onResult(AccessToken result) {
//                Log.i("wtf", "AccessToken->" + result.getAccessToken());
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(DemoApplication.this, "启动成功", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onError(FaceError error) {
//                Log.e("xx", "AccessTokenError:" + error);
//                error.printStackTrace();
//
//            }
//        }, this, Config.apiKey, Config.secretKey);
    }
}

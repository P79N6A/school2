/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.freshtribes.icecream.baiduai;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import android.content.Context;
import android.util.Log;

import com.freshtribes.icecream.baiduai.model.AccessToken;
import com.freshtribes.icecream.baiduai.model.DynamicParams;
import com.freshtribes.icecream.baiduai.model.FaceModel;
import com.freshtribes.icecream.baiduai.model.RegParams;
import com.freshtribes.icecream.baiduai.parser.IdentifyParser;
import com.freshtribes.icecream.baiduai.parser.OnlineLivenessResultParser;
import com.freshtribes.icecream.baiduai.parser.RegResultParser;
import com.freshtribes.icecream.baiduai.parser.UploadParser;
import com.freshtribes.icecream.baiduai.parser.UserListParser;
import com.freshtribes.icecream.baiduai.parser.VerifyParser;
import com.freshtribes.icecream.baiduai.utils.DeviceUuidFactory;
import com.freshtribes.icecream.baiduai.utils.HttpUtil;
import com.freshtribes.icecream.baiduai.utils.LogUtil;
import com.freshtribes.icecream.baiduai.utils.OnResultListener;

public class APIService {

    private static final String BASE_URL = "https://aip.baidubce.com";

    private static final String ACCESS_TOEKN_URL = BASE_URL + "/oauth/2.0/token?";

    private static final String REG_URL = BASE_URL + "/rest/2.0/face/v2/faceset/user/add";
    // 在线活体接口
    private static final String ONLINE_LIVENESS_URL = BASE_URL + "/rest/2.0/face/v2/faceverify";

    private static final String DELETE_GROUP_USERS = BASE_URL + "/rest/2.0/face/v2/faceset/group/deleteuser";

    private static final String IDENTIFY_URL = BASE_URL + "/rest/2.0/face/v2/identify";
    private static final String VERIFY_URL = BASE_URL + "/rest/2.0/face/v2/verify";
    private static final String FIND_USERS_IN_GROUP_URL = BASE_URL + "/rest/2.0/face/v2/faceset/group/getusers";
    private static final String DEL_USER_URL = BASE_URL + "/rest/2.0/face/v2/faceset/user/delete";

    private String accessToken;

    private String groupId;

    private APIService() {

    }

    private static volatile APIService instance;

    public static APIService getInstance() {
        if (instance == null) {
            synchronized (APIService.class) {
                if (instance == null) {
                    instance = new APIService();


                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        // 采用deviceId分组
        HttpUtil.getInstance().init();
        DeviceUuidFactory.init(context);
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 明文aksk获取token
     *
     * @param listener
     * @param context
     * @param ak
     * @param sk
     */
    public void initAccessTokenWithAkSk(final OnResultListener<AccessToken> listener, Context context, String ak,
                                        String sk) {

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(ak);
        sb.append("&client_secret=").append(sk);
        sb.append("&grant_type=client_credentials");
        HttpUtil.getInstance().getAccessToken(listener, ACCESS_TOEKN_URL, sb.toString());

    }

    public void reg(OnResultListener listener, ArrayList<File> files, String uid, String username) {
        RegParams params = new RegParams();
        params.setGroupId(groupId);
        LogUtil.i("wtf", "reg uid->" + uid);

        params.setUid(uid);
        params.setUserInfo(username);
        for (File file : files) {
            params.setImageFile(file);
        }

        RegResultParser parser = new RegResultParser();
        HttpUtil.getInstance().post(urlAppendCommonParams(REG_URL), params, parser, listener);
    }

    public void reg(OnResultListener listener, File file, String uid, String username) {
        RegParams params = new RegParams();
        params.setGroupId(groupId);

        params.setUid(uid);
        params.setUserInfo(username);
        params.setImageFile(file);

        RegResultParser parser = new RegResultParser();
        String url = urlAppendCommonParams(REG_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    public void reg_chen(OnResultListener listener, File file, String uid, String username,String groupid,String token) {
        RegParams params = new RegParams();
        params.setGroupId(groupid);

        params.setUid(uid);
        params.setUserInfo(username);
        params.setImageFile(file);

        RegResultParser parser = new RegResultParser();
        String url = REG_URL+"?access_token="+token;

        Log.i("icecream","url:"+url);

        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    /**
     * 在线活体检测接口，放在视频伪造，传活体得的图片，
     * 下面是活体检测接口，如果对安全性有要求的话，请使用下面注释部分（请发工单申请权限，或者联系商务接口人）
     *
     * @param listener
     * @param file
     */
    public void onlineLiveness(OnResultListener listener, File file) {
        DynamicParams params = new DynamicParams();
        params.putParam("face_fields", "qualities,faceliveness");
        params.putFile("image", file);
        OnlineLivenessResultParser parser = new OnlineLivenessResultParser();
        HttpUtil.getInstance().post(urlAppendCommonParams(ONLINE_LIVENESS_URL), "image", params, parser, listener);
    }

    /**
     * @param listener
     * @param file
     */

    public void identify(OnResultListener<FaceModel> listener, File file) {
        DynamicParams params = new DynamicParams();
        params.putParam("group_id", groupId);
        params.putFile("image", file);
        params.putParam("ext_fields", "faceliveness");

        IdentifyParser parser = new IdentifyParser();
        String url = urlAppendCommonParams(IDENTIFY_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    public void identify_chen(OnResultListener<FaceModel> listener, File file, String groupid,String token) {

        DynamicParams params = new DynamicParams();
        params.putParam("group_id", groupid);
        params.putFile("image", file);
        params.putParam("ext_fields", "faceliveness");

        IdentifyParser parser = new IdentifyParser();
        //String url = urlAppendCommonParams(IDENTIFY_URL);
        String url = IDENTIFY_URL+"?access_token="+token;
        HttpUtil.getInstance().post(url, params, parser, listener);
    }


    public void verify(OnResultListener<FaceModel> listener, File file, String uid) {
        DynamicParams params = new DynamicParams();
        params.putParam("group_id", groupId);
        params.putFile("image", file);
        params.putParam("uid", uid);
        params.putParam("ext_fields", "faceliveness");


        VerifyParser parser = new VerifyParser();
        String url = urlAppendCommonParams(VERIFY_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    public void upload(OnResultListener<Integer> listener, File file, HashMap<String, String> extraParams) {
        UUID uuid = DeviceUuidFactory.getUuid();
        DynamicParams params = new DynamicParams();
//        params.putParam("deviceId", "DS-2CD5A32EFWD-IZ20170412AACH734237291");
        params.putParam("deviceId", "DS-2CD5A32EFWD20170317AACH734025525");
        // 20170317AACH734025525
        params.putParam("tabId", uuid.toString());
        params.putParam("createdTime", String.valueOf(System.currentTimeMillis()));
        if (extraParams != null) {
            for (String p : extraParams.keySet()) {
                params.putParam(p, extraParams.get(p));
            }
        }

        params.putFile(file.getName(), file);

        UploadParser parser = new UploadParser();
        String url = "http://api.sit.ffan.com/faceperception/v1/user/identify";
        HttpUtil.getInstance().post(url, "imgStr", params, parser, listener);
    }


    public void findGroupUsers(OnResultListener<List<FaceModel>> listener) {
        DynamicParams params = new DynamicParams();
        params.putParam("group_id", groupId);
        UserListParser parser = new UserListParser();
        String url = urlAppendCommonParams(FIND_USERS_IN_GROUP_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }


    public void delUidInGroup(String uid, OnResultListener listener) {
        DynamicParams params = new DynamicParams();
        // params.putParam("group_id", groupId);
        params.putParam("uid", uid);
        //        params.putParam("group_id", groupId);


        RegResultParser parser = new RegResultParser();
        String url = urlAppendCommonParams(DEL_USER_URL);
        HttpUtil.getInstance().post(url, params, parser, listener);
    }

    /**
     * URL append access token，sdkversion，aipdevid
     *
     * @param url
     * @return
     */
    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?access_token=").append(accessToken);

        return sb.toString();
    }

}

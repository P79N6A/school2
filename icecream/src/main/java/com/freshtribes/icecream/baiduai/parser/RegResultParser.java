/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.freshtribes.icecream.baiduai.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.freshtribes.icecream.baiduai.exception.FaceError;
import com.freshtribes.icecream.baiduai.model.RegResult;

import android.util.Log;

public class RegResultParser implements Parser<RegResult> {


    @Override
    public RegResult parse(String json) throws FaceError {
        Log.e("xx", "oarse:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceError error = new FaceError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                throw error;
            }

            RegResult result = new RegResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceError error = new FaceError(FaceError.ErrorCode.JSON_PARSE_ERROR, "Json parse error:" + json, e);
            throw error;
        }
    }
}

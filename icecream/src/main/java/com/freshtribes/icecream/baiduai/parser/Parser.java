/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.freshtribes.icecream.baiduai.parser;

import com.freshtribes.icecream.baiduai.exception.FaceError;

/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceError;
}

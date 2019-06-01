package com.mnevent.hz.Utils;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;


public class OssUtil {
    private static final String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
    private static final String mAccessKeyId = "LTAIxHdwnOvg2H6o";
    private static final String mAccessKeySecret = "MnHW1C8SIhBAvW0U5jYouJlDzg7b4i";
    private static final String mLogsBucket = "freshmaclog"; //oss上待上传的文件夹名称
    private static final OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(mAccessKeyId, mAccessKeySecret);
    //oss链接  http://freshmaclog.oss-cn-shanghai.aliyuncs.com/829AVM000123_20181229123409.zip
    private static final String mUrl = "http://freshmaclog.oss-cn-shanghai.aliyuncs.com/";

   /* *
     * @param context
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param timeStr  参数值
     * */

    public static void uploadFile(Context context, final String fileName, String filePath, final String timeStr) {
        OSS oss = new OSSClient(context, endpoint, credentialProvider);
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(mLogsBucket, fileName, filePath);
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                //mUrl+fileName   为完整的文件下载url
                try {
                    LogUtils.getInstance().d("日志上传成功:" + mUrl + fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
        try {
            task.getResult();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }


}


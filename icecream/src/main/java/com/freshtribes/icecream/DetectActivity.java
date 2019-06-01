package com.freshtribes.icecream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.FaceDetector;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.FaceFilter;
import com.baidu.aip.face.ImageFrame;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.face.camera.PermissionCallback;
import com.baidu.idl.facesdk.FaceInfo;
import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.baiduai.APIService;
import com.freshtribes.icecream.baiduai.exception.FaceError;
import com.freshtribes.icecream.baiduai.model.FaceModel;
import com.freshtribes.icecream.baiduai.utils.ImageUtil;
import com.freshtribes.icecream.baiduai.utils.OnResultListener;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class DetectActivity extends Activity {
    private final String TAG = "leak_detect";
    private TextView nameTextView;
    // 预览View;
    private PreviewView previewView;
    // textureView用于绘制人脸框等。
    private TextureView textureView;
    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;

    private ImageView closeIv;

    // 为了方便调式。
    private ImageView testView;
    private Handler handler = new Handler();

    private SimpleDateFormat formathmsSSS = new SimpleDateFormat("HH:mm:ss:SSS", Locale.CHINA);
    private SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    private boolean isCountDownRunning = true;

    private String uid = "icecream";
    private String token = "";
//    private Bitmap bitmap;

    private long onCreateTime = 0l;
    private long firstTestImage = 0l;

    Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        Log.i(TAG,"打开DetectActivity:"+formathmsSSS.format(System.currentTimeMillis()));
        onCreateTime = System.currentTimeMillis();

        faceDetectManager = new FaceDetectManager(getApplicationContext());
        testView = (ImageView) findViewById(R.id.test_view);
        nameTextView = (TextView) findViewById(R.id.name_text_view);
        previewView = (PreviewView) findViewById(R.id.preview_view);
        textureView = (TextureView) findViewById(R.id.texture_view);


        closeIv = (ImageView) findViewById(R.id.closeIv);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCountDownRunning = false;

                Intent intent = new Intent();
                intent.putExtra("code", "1");
                intent.putExtra("msg","");
                setResult(Activity.RESULT_OK, intent);
                DetectActivity.this.finish();
            }
        });

        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280, 720);
        // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
        FaceDetector.getInstance().setMinFaceSize(120);
        FaceDetector.getInstance().setNumberOfThreads(4);
        // 设置预览
        cameraImageSource.setPreviewView(previewView);

        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        // 设置人脸过滤角度，角度越小，人脸越正，比对时分数越高
        faceDetectManager.getFaceFilter().setAngle(20);

        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {

                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
//                if(bitmap!=null){
//                    testView.setImageBitmap(null);
//                    if(!bitmap.isRecycled()){
//                        bitmap.recycle();
//                        Log.i(TAG,"回收bitmap对象");
//                    }
//                    bitmap = null;
//                }
//                final Bitmap bitmap = Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(), Bitmap.Config
//                                .ARGB_8888);
//                Log.i(TAG,"生成bitmap对象");
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        testView.setImageBitmap(bitmap);
//
//                        if(firstTestImage == 0l) {
//                            firstTestImage = System.currentTimeMillis() - onCreateTime;
//                            Log.i(TAG, "首次看到testview（毫秒）：" + firstTestImage);
//                        }
//                    }
//                });
                if (infos == null) {
                    // null表示，没有人脸。
                    Log.e(TAG,"onDetectFace 显示出来");
                    showFrame(null);
                    shouldUpload = true;
                }
            }
        });
        // 人脸追踪回调。没有人脸时不会回调。
        faceDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
            @Override
            public void onTrack(FaceFilter.TrackedModel trackedModel) {
                Log.e(TAG,"onTrack 显示出来");
                showFrame(trackedModel);
                //Log.i(TAG,"有人脸过来了......");
                if (trackedModel.meetCriteria()) {
                    // 该帧符合过虑标准，人脸质量较高。上传至服务器，进行识别
                    upload(trackedModel);
                }
            }
        });

        // 安卓6.0+ 运行时，权限回调。
        cameraImageSource.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(DetectActivity.this,
                        new String[] {android.Manifest.permission.CAMERA}, 100);
                return true;
            }
        });

        textureView.setOpaque(false);

        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }

        setCameraType(cameraImageSource);

        // 启动一个线程，来显示倒计时，以及倒计时完成后，关闭此窗口
        isCountDownRunning = true;
        Thread countDownThread = new CountDown_Self();
        countDownThread.setPriority(Thread.MIN_PRIORITY);
        countDownThread.setName("人脸支付倒计时线程：" + formathms.format(new Date(System.currentTimeMillis())));
        countDownThread.start();

        new Thread(){
            public void run(){
                for(int i=0;i<3;i++) {
                    long timestamp = System.currentTimeMillis();
                    String str = "macid=" + ((MyApp) getApplication()).getMainMacID() + "&timestamp=" + timestamp + "&accesskey=" + ((MyApp) getApplication()).getAccessKey();
                    String md5 = MD5.GetMD5Code(str);

                    String rtnstr = (new MyHttp()).post(((MyApp) getApplication()).getServerurl() + "/baiduaccesstoken",
                            "macid=" + ((MyApp) getApplication()).getMainMacID() + "&timestamp=" + timestamp + "&md5=" + md5);
                    LogUtils.i("baiduaccesstoken,取得token的返回值rtnstr:" + rtnstr);

                    if (rtnstr.length() > 0) {
                        try {
                            JSONObject soapJson = new JSONObject(rtnstr);
                            token = soapJson.getString("token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (token.length() > 0) return;
                }
            }
        }.start();

//        new Thread(){
//            public void run(){
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((TextView)findViewById(R.id.name_text_view)).setText("正在打开摄像头0.5");
//                    }
//                });
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((TextView)findViewById(R.id.name_text_view)).setText("正在打开摄像头1.0");
//                    }
//                });
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((TextView)findViewById(R.id.name_text_view)).setText("正在打开摄像头1.5");
//                    }
//                });
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((TextView)findViewById(R.id.name_text_view)).setText("正在打开摄像头2.0");
//                    }
//                });
//            }
//        }.start();
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO 选择使用前置摄像头
//         cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);

        // TODO 选择使用usb摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        // 如果不设置，人脸框会镜像，显示不准
        previewView.getTextureView().setScaleX(-1);

        // TODO 选择使用后置摄像头
//      cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//      previewView.getTextureView().setScaleX(-1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开始检测
        faceDetectManager.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 结束检测。
        faceDetectManager.stop();
        // 释放相机内存
        //cameraImageSource.stop();
        //Log.i(TAG,"cameraImageSource.stop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        testView.setImageBitmap(null);
//        if(bitmap!=null){
//            if(!bitmap.isRecycled()) {
//                bitmap.recycle();
//                Log.i(TAG,"回收bitmap对象 onDestroy");
//            }
//            bitmap = null;
//        }

        previewView = null;

        faceDetectManager.stop();

    }

    // 屏幕上显示用户信息。
    private void showUserInfo(FaceModel model) {
        if (model != null) {
            // 把userInfo和分数显示在屏幕上
            //String text = String.format(Locale.ENGLISH, "%s  %.2f", model.getUserInfo(), model.getScore());
            //nameTextView.setText(text);
            if(model.getScore() < 80.00){
                String text = String.format(Locale.ENGLISH, "人脸识别得分：%.2f，请靠近点！", model.getScore());
                nameTextView.setText(text);

                Log.e(TAG,text);
            } else {
                String text = String.format(Locale.ENGLISH, "人脸识别得分：%.2f", model.getScore());
                nameTextView.setText(text);
            }
        }
    }

    private boolean shouldUpload = true;

    // 上传一帧至服务器进行，人脸识别。
    private void upload(FaceFilter.TrackedModel model) {
        if (model.getEvent() != FaceFilter.Event.OnLeave) {
            if (!shouldUpload) {
                return;
            }
            shouldUpload = false;
            final Bitmap face = model.cropFace();
            try {
                final File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                // 人脸识别不需要整张图片。可以对人脸区别进行裁剪。减少流量消耗和，网络传输占用的时间消耗。
                ImageUtil.resize(face, file, 200, 200);
                APIService.getInstance().identify_chen(new OnResultListener<FaceModel>() {
                    @Override
                    public void onResult(FaceModel result) {
                        if(result == null){
                            isCountDownRunning = false;
                            //百度返回onResponse json->{"error_code":216402,"error_msg":"face not found","log_id":3773479823050408}
                            Intent intent = new Intent();
                            intent.putExtra("code", "1");
                            intent.putExtra("msg","人脸校验不通过,请确认是否已注册");
                            setResult(Activity.RESULT_OK, intent);
                            //Toast.makeText(DetectLoginActivity.this, "人脸校验不通过,请确认是否已注册", Toast.LENGTH_SHORT).show();
                            DetectActivity.this.finish();
                            return;
                        }

                        LogUtils.i("人脸识别的返回，得分："+result.getScore());
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                        if (result == null) {
                            return;
                        }
                        // 识别分数小于80，也可能是角度不好。可以选择重试。
                        if (result.getScore() < 80) {
                            shouldUpload = true;
                        } else {
                            isCountDownRunning = false;
                            Intent intent = new Intent();
                            intent.putExtra("code", "0");
                            intent.putExtra("msg","");
                            intent.putExtra("user_info", result.getUserInfo());
                            intent.putExtra("uid", result.getUid());
                            intent.putExtra("score", result.getScore());
                            setResult(Activity.RESULT_OK, intent);
                            DetectActivity.this.finish();
                            return;
                        }
                        showUserInfo(result);
                    }

                    @Override
                    public void onError(FaceError error) {
                        error.printStackTrace();
                        shouldUpload = true;
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                    }
                }, file,uid,token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            shouldUpload = true;
        }
    }

    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }

    RectF rectF = new RectF();

    /**
     * 绘制人脸框。
     *
     * @param model 追踪到的人脸
     */
    private void showFrame(FaceFilter.TrackedModel model) {

        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            return;
        }
        // 清空canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (model != null) {
            model.getImageFrame().retain();
            rectF.set(model.getFaceRect());

            // 检测图片的坐标和显示的坐标不一样，需要转换。
            if(previewView != null) {
                previewView.mapFromOriginalRect(rectF);
                if (model.meetCriteria()) {
                    // 符合检测要求，绘制绿框
                    paint.setColor(Color.GREEN);
                } else {
                    // 不符合要求，绘制黄框
                    paint.setColor(Color.YELLOW);

                    String text = "请正视摄像头";
                    float width = paint.measureText(text) + 50;
                    float x = rectF.centerX() - width / 2;
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawText(text, x + 25, rectF.top - 20, paint);
                    paint.setColor(Color.YELLOW);
                }
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
            }
        }
        textureView.unlockCanvasAndPost(canvas);
    }


    // 倒计时的线程
    private class CountDown_Self extends Thread{
        public void run(){
            while(isCountDownRunning){
                try{Thread.sleep(500);}catch (Exception e){e.printStackTrace();}

                String app_pay_countdown = ((MyApp)getApplication()).getPay_count();
                final String disp_pay_countdown = "" + (Integer.parseInt(app_pay_countdown) - 5);
                if(disp_pay_countdown.equals("0")){
                    isCountDownRunning = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.putExtra("code", "1");
                            intent.putExtra("msg","人脸识别已超时");
                            setResult(Activity.RESULT_OK, intent);
                            DetectActivity.this.finish();
                        }
                    });
                }
                if(isCountDownRunning){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.countdowntv)).setText(disp_pay_countdown);
                        }
                    });
                }
            }
        }
    }
}

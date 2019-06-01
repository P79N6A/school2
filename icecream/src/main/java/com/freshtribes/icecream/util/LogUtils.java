package com.freshtribes.icecream.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LogUtils {

    public static String customTagPrefix = "icecream"; // 自定义Tag的前缀

    private static SimpleDateFormat formatymd = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);


    protected static PrintWriter logWritter = null;

    protected static LogUtils writer = null;

    // 日志文件名称
    protected static String fileName = "";

    // 当前日志的日期
    protected static String currendDate = "";

    private static String TAG = "LogUtils";
    private static final SimpleDateFormat formatymdthms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

    private LogUtils() {
            File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/mylogs/");
            //Log.i("LogUtils", "" + file.isDirectory());
            if(!folder.exists()){
                boolean isfoldok = folder.mkdirs();
                Log.i(TAG,  "文件夹" + Environment.getExternalStorageDirectory() + "/mylogs/" + "的mkdir结果:" + isfoldok);
            } else {
                // 如果已经存在,那么查看所有的文件,超过7个文件,那么要排序,删除旧的,7个之外的文件
                Log.i(TAG,"文件夹已经存在了...");

                File[] files = folder.listFiles();
                Log.i(TAG,"文件的个数：" + files.length);

                if(files.length > 7) {

                    List<String> items = new ArrayList<String>();

                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        items.add(file.getName());
                    }
                    Collections.sort(items, String.CASE_INSENSITIVE_ORDER);

                    for(int i=0;i<items.size()-7;i++){
                        File delfile = new File(Environment.getExternalStorageDirectory() + "/mylogs/" + items.get(i));
                        delfile.delete();
                    }
                }
            }
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(Line:%d)"; // 占位符

        String callerClazzName = caller.getClassName(); // 获取到类名
        //Log.i("---",callerClazzName);  com.mengdeman.serialtest.MainActivity
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);

        tag = String.format(tag, callerClazzName, caller.getMethodName(),caller.getLineNumber()); // 替换
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;

        return tag;
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    public static void v(String content) {

        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        Log.v(tag, content);

        // 写入文件中去
        try {
            getInstance().writeLockLogFile("v/" + tag, content);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void d(String content) {

        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        Log.d(tag, content);

        // 写入文件中去
        try {
            getInstance().writeLockLogFile("d/" + tag, content);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void i(String content) {

        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        Log.i(tag, content);

        // 写入文件中去
        try {
            getInstance().writeLockLogFile("i/" + tag, content);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void e(String content) {

        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);

        Log.e(tag, content);

        // 写入文件中去
        try {
            getInstance().writeLockLogFile("e/" + tag, content);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void writeLockLogFile(String tag,String content){

        synchronized (logWritter) {
            logWritter.println(formatymdthms.format(new Date()) + ":" + tag + ": " + content);
            logWritter.flush();
        }
    }

    public static synchronized LogUtils getInstance() throws Exception {
        if (writer == null) {
            writer = new LogUtils();

            currendDate = formatymd.format(new Date());
            fileName = Environment.getExternalStorageDirectory().getPath() + "/mylogs/" + currendDate + ".log";


            logWritter = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(
                                            fileName, true), "UTF-8")));

        }

        String now = formatymd.format(new Date());

        if(!currendDate.equals(now)) {
            // 日期发生变化了
            currendDate = now;

            fileName = Environment.getExternalStorageDirectory().getPath() + "/mylogs/" + currendDate + ".log";

            logWritter = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(
                                            fileName, true), "UTF-8")));

        }

        return writer;
    }

    public static void closePrintWritter(){
        try {
            getInstance().close();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    private void close(){
        try {
            synchronized (logWritter) {
                logWritter.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
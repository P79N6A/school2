package com.freshtribes.icecream.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class MyExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;
    
    private Context mContext;
    
    private String crashinfo = "";

	public static String macid = "";
	public static String pkgname = "";
	public static String version = "";

    public MyExceptionHandler(Context context) {
    	mContext = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

    	StringWriter sw = new StringWriter(); 
        PrintWriter pw = new PrintWriter(sw); 
        ex.printStackTrace(pw); 
         
        StringBuilder sb = new StringBuilder(); 
        sb.append(sw.toString()); 
        
        Log.e("System.err",sb.toString());

        try{
        	crashinfo = URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        new Thread() {  
        	public void run(){
	        	String urlDate="http://mac.freshtribes.com/crash/crash?macid=" + macid + "&pkgname=" + pkgname + version + "&info="+crashinfo;
	            try {  
	                //封装访问服务器的地址  
	            	URL url=new URL(urlDate);  
	                try {  
	                    //打开对服务器的连接  
	                	HttpURLConnection conn=(HttpURLConnection) url.openConnection();  
	                    //连接服务器  
	                    conn.connect();  
	                    
	                    InputStreamReader in = new InputStreamReader(conn.getInputStream());
	                    BufferedReader bufferedReader = new BufferedReader(in);
	                    StringBuffer strBuffer = new StringBuffer();
	                    String line = null;
	                    while ((line = bufferedReader.readLine()) != null) {
	                        strBuffer.append(line);
	                    }
	                    Log.i("return:",strBuffer.toString());
	
	                } catch (IOException e) {  
	                    //e.printStackTrace();  
	                }  
	            } catch (Exception e) {  
	                //e.printStackTrace();  
	            }  
        	}
        }.start(); 
        
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//Log.e(TAG, "error : ", e);
		}

		// 退出程序
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);

    }
    
}
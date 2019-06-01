package com.mnevent.hz.Utils;

import android.util.Log;

import com.mnevent.hz.App.MyApp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android_serialport_api.SerialPort;

/**
 * Created by zyand on 2019/1/8.
 * 打印
 */

public class PrintUtils {

    // 打印
    public static void PrintThreadMiddle(boolean isbar,String line1,String line2,String line3) {
        Log.d("zlc","执行打印"+line1+";"+line2+";"+line3);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        byte[] fa_cmd_rtn = new byte[]{ (byte) 0x0A};
        byte[] fa_cmd_cut = new byte[]{ (byte) 0x1B,(byte) 0x69};

        byte[] fa_cmd_left = new byte[]{ (byte) 0x1B,(byte) 0x61,(byte) 0x00};
        byte[] fa_cmd_center = new byte[]{ (byte) 0x1B,(byte) 0x61,(byte) 0x01};

        byte[] fa_cmd_font_standard = new byte[]{ (byte) 0x1B,(byte) 0x21,(byte) 0x00};
        byte[] fa_cmd_font_rar = new byte[]{ (byte) 0x1B,(byte) 0x21,(byte) 0x01};

        byte[] fa_cmd_font_size_normal = new byte[]{(byte) 0x1D,(byte) 0x21,(byte) 0x00};
        byte[] fa_cmd_font_high_twice = new byte[]{(byte) 0x1D,(byte) 0x21,(byte) 0x01};   // 01-02-03-04-05
        byte[] fa_cmd_font_width_twice = new byte[]{(byte) 0x1D,(byte) 0x21,(byte) 0x10};   // 10-20-30-40-50

        try {
            SerialPort mSerialPort = new SerialPort(new File("/dev/ttyUSB0"), 9600, 0, 8,1,110 );
            //恢复初始大小字体
            mSerialPort.getOutputStream().write(fa_cmd_font_standard);
            mSerialPort.getOutputStream().write(fa_cmd_font_size_normal);
            Log.d("zlc","打开串口");
            if(isbar){
                mSerialPort.getOutputStream().write(fa_cmd_center);
                mSerialPort.getOutputStream().write("--------------".getBytes("GBK"));
                mSerialPort.getOutputStream().write(fa_cmd_rtn);
                mSerialPort.getOutputStream().write(fa_cmd_left);
            }

            mSerialPort.getOutputStream().write(line1.getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(line2.getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(line3.getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);




            mSerialPort.getOutputStream().close();
            mSerialPort.close();
        } catch (SecurityException e) {
            ToastUtil.showToast(MyApp.mApplication," 打开串口失败open fail" + e.getMessage());
        } catch (IOException e) {
            ToastUtil.showToast(MyApp.mApplication," 打开串口失败rw fail" + e.getMessage());
        }
    }




    // 打印
    public static void  PrintThreadEnd(String name, String count, String money, String tele){
        byte[] fa_cmd_rtn = new byte[]{ (byte) 0x0A};
        byte[] fa_cmd_cut = new byte[]{ (byte) 0x1B,(byte) 0x69};

        byte[] fa_cmd_left = new byte[]{ (byte) 0x1B,(byte) 0x61,(byte) 0x00};
        byte[] fa_cmd_center = new byte[]{ (byte) 0x1B,(byte) 0x61,(byte) 0x01};

        byte[] fa_cmd_font_standard = new byte[]{ (byte) 0x1B,(byte) 0x21,(byte) 0x00};
        byte[] fa_cmd_font_rar = new byte[]{ (byte) 0x1B,(byte) 0x21,(byte) 0x01};

        byte[] fa_cmd_font_size_normal = new byte[]{(byte) 0x1D,(byte) 0x21,(byte) 0x00};
        byte[] fa_cmd_font_high_twice = new byte[]{(byte) 0x1D,(byte) 0x21,(byte) 0x01};   // 01-02-03-04-05
        byte[] fa_cmd_font_width_twice = new byte[]{(byte) 0x1D,(byte) 0x21,(byte) 0x10};   // 10-20-30-40-50

        try {
            SerialPort mSerialPort = new SerialPort(new File("/dev/ttyUSB0"), 9600, 0, 8,1,110);
            //恢复初始大小字体
            mSerialPort.getOutputStream().write(fa_cmd_font_standard);
            mSerialPort.getOutputStream().write(fa_cmd_font_size_normal);

            mSerialPort.getOutputStream().write("-------------------------------".getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(("name："+name).getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(("A total number："+count+"piece").getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(("total amount $："+money).getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);

            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_center);
            mSerialPort.getOutputStream().write("***Thanks for your patronage.***".getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write("[Be sure to read the instructions carefully]".getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write("[We do not accept any refund except for quality problem]".getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);

            mSerialPort.getOutputStream().write(fa_cmd_left);
            mSerialPort.getOutputStream().write(("phone："+tele).getBytes("GBK"));
            mSerialPort.getOutputStream().write(fa_cmd_rtn);


            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_rtn);
            mSerialPort.getOutputStream().write(fa_cmd_cut);

            mSerialPort.getOutputStream().close();
            mSerialPort.close();
        } catch (SecurityException e) {
            ToastUtil.showToast(MyApp.mApplication," 打开串口失败open fail" + e.getMessage());
        } catch (IOException e) {
            ToastUtil.showToast(MyApp.mApplication," 打开串口失败rw fail" + e.getMessage());
        }
    }
}

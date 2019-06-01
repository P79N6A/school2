package com.zhongyagroup.school.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hcReader.AndroidUSB;
import com.hcReader.Reader;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class ICCard {

    //扇区（第2扇区：8-11模块）
    private String target_section = "2";
    //编码模块
    private String mod_innerno = "8";
    //密码模块
    private String mod_pwd = "11";

    private UsbManager manager = null;
    private Reader reader = null;
    private String Device_USB = "com.android.example.USB";
    private UsbDevice usbDevice = null;

    public boolean open(Context context){
        // 获取所有USB连接的设备列表
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        if (manager == null) {
            LogUtils.e("无法获取所有USB连接的设备列表");
            return false;
        } else {
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            //Log.i(TAG,"USB连接的设备数："+deviceList.size());
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while(deviceIterator.hasNext()){
                UsbDevice device = deviceIterator.next();
                //Log.i(TAG,""+device.getDeviceName());
            }
        }

        // 找到特定的USB读写器设备
        reader = new AndroidUSB(context, manager);
        usbDevice = reader.GetUsbReader();
        if (usbDevice == null) {
            LogUtils.e("未找到USB连接的读写器设备");
            return false;
        }

        // 判断是否拥有该设备的连接权限
        if (!manager.hasPermission(usbDevice)) {
            // 如果没有则请求权限
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(Device_USB), PendingIntent.FLAG_UPDATE_CURRENT);
            manager.requestPermission(usbDevice, mPermissionIntent);
        } else {
            short st = reader.OpenReader(usbDevice);
            if (st >= 0) {
                return true;
            } else {
                LogUtils.e("读写器连接失败!");
                return false;
            }
        }

        return false;
    }

    public void close(){
        // 读写器关闭
        if(reader != null) {
            reader.hc_exit();
        }
    }

    public String getICCardInnerNo(String strpwd){
        // （1）选定卡
        // mode - 0 -- IDLE mode, 只有处在IDLE 状态的卡片才响应读写器的命令;
        // 1 -- ALL mode, 处在 IDLE 状态和HALT 状态的卡片都将响应读写器的命令
        byte mode = (byte)1;
        byte[] bysnr = new byte[4];
        Arrays.fill(bysnr, (byte) 0x00);
        short st = reader.rf_card(mode,bysnr);
        if(st < 0){
            //ToastUtil.showToast(MainActivity.this, reader.GetErrMessage(st)+",请确认卡是否存在！如已存在请重新放置！");
            //tvStartInfo.setText(reader.GetErrMessage(st)+",请确认卡是否存在！如已存在请重新放置！");
            return "";
        }

        // （2）先判断是不是空白卡
        byte keymode = (byte)0;
        byte[] btPwd = reader.hexStringToBytes("FFFFFFFFFFFF");
        st = reader.rf_authentication(keymode, Byte.parseByte(target_section), btPwd);
        if (st >= 0) {
            //ToastUtil.showToast(MainActivity.this, "这是一张空白卡！");
            //tvStartInfo.setText("这是一张空白卡！");
            return "";
        }

        // （3）不是空白卡时，用预设的整体密码，先验证密码。
        // * 注意，需要再次选定卡
        reader.rf_card(mode,bysnr);
        byte[] btPwdAll = reader.hexStringToBytes(strpwd+"FF078069FFFFFFFFFFFF");
        short st4 = reader.rf_authentication(keymode, Byte.parseByte(target_section), btPwdAll);
        if (st4 < 0) {
            //ToastUtil.showToast(MainActivity.this, reader.GetErrMessage(st4)+",请确认是不是自己发行的卡。");
            //tvStartInfo.setText(reader.GetErrMessage(st4)+",请确认是不是自己发行的卡。");
            return "";
        }

        // （4）验证密码通过了，读取数据
        byte[] data = new byte[16];
        short st6 = reader.rf_read(Byte.parseByte(mod_innerno), data);
        if (st6 >= 0) {
            //ToastUtil.showToast(MainActivity.this, reader.bytesToHexString(data, 0, st6));
            // Log.d("zlc", reader.bytesToHexString(data, 0, st6));
            String s = Stringand16int.hexStr2Str(reader.bytesToHexString(data, 0, st6)).substring(0, 10);
            reader.dv_beep((short) 40);
            //tvCardInnerNo.setText("设置的编号："+ s+"(前4位代表运营商编号)");
            return s;
        } else {
            //ToastUtil.showToast(MainActivity.this, reader.GetErrMessage(st6));
            //tvStartInfo.setText(reader.GetErrMessage(st6));

            return "";
        }
    }

    public void beep(){
        reader.dv_beep((short) 40);
    }

    public String getSerialNo(){
        byte mode = (byte)1;
        short st = reader.rf_request(mode);
        if(st < 0){
            return "";
        } else {
            // 大于等于0 时为卡片类型.
            // 0x0004 -- Mifare std. 1k(S50);
            // 0x0002 -- Mifare std. 4k(S70);
            // 0x0044 -- UltraLight;
            // 0x0010 -- MIFARE LIGHT;
            // 0x0008 -- MIFARE PRO; 0
            // x0005-- FM005;
            // 0x3300 -- SHC1102.
            LogUtils.i("卡片类型值："+st);
        }

        // 序列号 4个字节，读取内容
        byte[] snr = new byte[4];
        Arrays.fill(snr, (byte) 0x00);
        short st1 = reader.rf_anticoll(snr);
        if (st1 >= 0) {
            String s = reader.bytesToHexString(snr, 0, st1);
            String substring1 = s.substring(0, 2);
            String substring2 = s.substring(2, 4);
            String substring3 = s.substring(4, 6);
            String substring4 = s.substring(6, 8);

            String sdisp = substring4 + substring3 + substring2 + substring1;
            String stenserial = new BigInteger(sdisp, 16).toString(10);

            return getLenStrBeforeZero(stenserial,10);
        } else {
            return "n";
        }
    }

    private String getLenStrBeforeZero(String str,int len) {
        if(str.length() < len) {
            String zero = "";
            for(int i=0;i<len-str.length();i++) {
                zero = zero + "0";
            }
            return zero + str;
        } else {
            return str.substring(0, len);
        }
    }
}

package android_serialport_api;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;


import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.Utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ComQRIC {

    // 时分秒
    private static final SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    // 接线的串口线位置，默认是这个
    private String portName = "/dev/ttyO3";

    // 串口对象
    private boolean isOpen = false;
    private SerialPort serialPort = null;

    // 输入与输出流
    private InputStream is = null;
    private OutputStream os = null;

    // 读取数据的线程
    private boolean readThreadisRunning = false;
    private ReadThread mReadThread;

    private String receivedASCII = "";

    // 数据位
    private int databits = 8;
    // 停止位
    private int stopbits = 1;

    private int iBaudRate = 9600;

    /**
     * 构造函数
     * @param portName 串口标记，如果是空，表示使用默认的串口
     */
    public ComQRIC(String portName){
        if(portName.length()>0) {
            this.portName = portName;
        }
    }

    /**
     * 打开串口
     */
    public void openSerialPort() {
        boolean ret = false;
        if (!isOpen) {
            try {
                // 9600赫兹，无校验的方式通讯
                serialPort = new SerialPort(new File(portName), iBaudRate, 0, databits, stopbits, 110);
                os = serialPort.getOutputStream();
                is = serialPort.getInputStream();
                ret = true;
                isOpen = true;

                LogUtils.i(portName + " 打开串口成功!");
            } catch (SecurityException e) {
                LogUtils.e(portName + " 打开串口失败open fail" + e.getMessage());
            } catch (IOException e) {
                LogUtils.e(portName + " 打开串口失败rw fail" + e.getMessage());
            }
        }

        // (2) 打开串口成功
        if (ret) {
            readThreadisRunning = true;
            mReadThread = new ReadThread();
            mReadThread.setName("读取扫码读取信息的串口线程：" + formathms.format(new Date(System.currentTimeMillis())));
            mReadThread.start();
        }
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {

        if (mReadThread != null) {
            readThreadisRunning = false;
            SystemClock.sleep(100);    //暂停0.1秒保证mReadThread线程结束
        }

        if (this.is != null) {
            try {
                this.is.close();
                this.is = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.os != null) {
            try {
                this.os.close();
                this.os = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serialPort != null) {
            serialPort.close();
            LogUtils.i(portName + " 关闭串口成功!");
        }

        isOpen = false;
    }

    private class ReadThread extends Thread {
        // 接收到的字节信息
        byte[] rxByteArray = null;
        // 临时变量：接收到的字节信息
        byte[] rxByteArrayTemp = null;

        @Override
        public void run() {
            super.run();

            while (readThreadisRunning) {
                byte ret[];
                byte data[] = new byte[1024];

                try {
                    if (is != null ) {
                        int bytes  = is.available();
                        if(bytes > 0){
                            bytes = is.read(data);
                            ret = new byte[bytes];

                            System.arraycopy(data, 0, ret, 0, bytes);

                            // 发现有信息后就追加到临时变量
                            rxByteArrayTemp = ArrayAppend(rxByteArrayTemp, ret,bytes);


                        }else{
                            // ret = new byte[0];
                            // 这次发现没有信息，如果以前有信息的，那就是我们要的数据
                            if(rxByteArrayTemp != null){
                                LogUtils.i("接受到数据："+rxByteArrayTemp);
                                rxByteArray = new byte[rxByteArrayTemp.length];
                                System.arraycopy(rxByteArrayTemp, 0, rxByteArray, 0, rxByteArrayTemp.length);

                                rxByteArrayTemp = null;

                                // 处理收到的数据
                                analysis(rxByteArray);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 每50个毫秒去读取数据
                SystemClock.sleep(50);

            }
        }
    }

    public String getReceivedASCII(){
        String temp = receivedASCII;
        receivedASCII = "";
        return temp;
    }

    public void closeLED(){
        byte[] fa_cmd = new byte[]{(byte) 0x55, (byte) 0xAA, (byte) 0x24, (byte) 0x01,(byte) 0x00, (byte) 0x00,(byte) 0xDA};
        tranSendData(fa_cmd);
    }

    public void openLED(){
        byte[] fa_cmd = new byte[]{(byte) 0x55, (byte) 0xAA, (byte) 0x24, (byte) 0x01,(byte) 0x00, (byte) 0x01,(byte) 0xDB};
        tranSendData(fa_cmd);
    }

    /**
     * 传递要发送的字节内容Get，Set，Stop/Resume,Push
     * @param tmp_sendbyte 要发送的数据
     * @return ""空值表示传递成功，有值就是出错了
     */
    private void tranSendData(byte[] tmp_sendbyte){

        try {
            if (os != null) {
                os.write(tmp_sendbyte, 0, tmp_sendbyte.length);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 分析收到的数据，不符合规则的直接抛弃
     * @param receive 收到的一帧
     */
    private void analysis(byte[] receive){
        int len = receive.length;
        String hex = bytesToHexString(receive,len);

        LogUtils.i("analysis要处理的数据,长度："+len+";内容="+hex);
        LogUtils.i("analysis要处理的数据,长度："+len+";内容="+hexStringToString(hex));
       // SystemClock.sleep(1000);
        String str = hexStringToString(hex);
        str = str.substring(2,str.length()-1);
        String str2 = new String(Base64.decode(str.getBytes(), Base64.DEFAULT));

        Intent intent = new Intent();
        intent.setAction("shipment");
        Bundle bundle = new Bundle();
        //传值
        bundle.putString("type",str2);
        intent.putExtras(bundle);
        MyApp.mApplication.sendBroadcast(intent);

        if(hex.equals("55AA24000000DB")){
            // 表示关灯 或者 开灯成功的意思
            return;
        }

        if(receive[len-1] == (byte)0x0D){
            // 说明最后一个是0x0D
            StringBuilder sbu2 = new StringBuilder();
            for(int k = 0;k < len-1;k++){
                sbu2.append((char)Integer.parseInt(hex.substring(k*2,k*2+2),16));
            }
            receivedASCII = sbu2.toString();
        }
    }

    /**
     * 16进制转换成为string类型字符串
     * @param s
     * @return
     */
    public String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }



    /**
     * 将源数组追加到目标数组
     *
     * @param byte_1 原来的
     * @param byte_2 要追加的
     * @param size 要追加的长度
     * @return 返回一个新的数组，包括了原数组1和原数组2
     */
    private byte[] ArrayAppend(byte[] byte_1, byte[] byte_2,int size)
    {
        // java 合并两个byte数组

        if (byte_1 == null && byte_2 == null)
        {
            return null;
        } else if (byte_1 == null)
        {
            byte[] byte_3=new byte[ size];
            System.arraycopy(byte_2, 0, byte_3, 0, size);
            return byte_3;
            //return byte_2;
        } else if (byte_2 == null)
        {
            byte[] byte_3=new byte[byte_1.length ];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            return byte_3;
            //return byte_1;
        } else
        {
            byte[] byte_3 = new byte[byte_1.length + size];
            System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
            System.arraycopy(byte_2, 0, byte_3, byte_1.length, size);
            return byte_3;
        }

    }

    /**
     * 转换字节为十六进制
     * @param src 原来
     * @param size 长度
     * @return 结果
     */
    private String bytesToHexString(byte[] src, int size) {
        String ret = "";
        if (src == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(src[i] & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            if(ret.length()>0) {
                ret =  ret + "" + hex;
            } else {
                ret = hex;
            }
        }
        return ret.toUpperCase(Locale.US);
    }
}

package android_serialport_api;

import android.os.SystemClock;
import android.util.Log;

import com.zhongyagroup.school.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ComButton {
    private static String TAG = "ComButton~~~";
    // 时分秒
    private static final SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    // 接线的串口线位置，默认是这个
    private String portName = "/dev/ttyS4";

    // 串口对象
    public static boolean isOpenning = false;
    private SerialPort serialPort = null;

    // 输入与输出流
    private InputStream is = null;
    private OutputStream os = null;

    // 读取数据的线程
    private boolean readThreadisRunning = false;
    private ReadThread mReadThread;

    private byte[] rxByteArray = null;// 接收到的字节信息

    private CallBack cb;

    /**
     * 构造函数
     * @param portName 串口标记，如果是空，表示使用默认的串口
     */
    public ComButton(String portName,CallBack cb){
        if(portName.length()>0) {
            this.portName = portName;
        }
        this.cb = cb;
    }

    /**
     * 打开串口
     */
    public void openSerialPort() {
        boolean ret = false;
        if (!isOpenning) {
            try {
                // 9600赫兹，无校验的方式通讯
                serialPort = new SerialPort(new File(portName), 9600, 0,1,SerialPort.PARITY_CHECK.NONE.ordinal());
                os = serialPort.getOutputStream();
                is = serialPort.getInputStream();
                ret = true;
                isOpenning = true;

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
            mReadThread.setName("读取机器的串口线程：" + formathms.format(new Date(System.currentTimeMillis())));
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

        isOpenning = false;
    }

    private class ReadThread extends Thread {
        // 接收到的字节信息
        //byte[] rxByteArray = null;
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
                            //LogUtils.i("ReadThread中发现串口有数据过来了,长度:" + bytes + ";内容=" + bytesToHexString(data,bytes));
                            System.arraycopy(data, 0, ret, 0, bytes);

                            // 发现有信息后就追加到临时变量
                            rxByteArrayTemp = ArrayAppend(rxByteArrayTemp, ret,bytes);

                        }else{
                            ret = new byte[0];
                            // 这次发现没有信息，如果以前有信息的，那就是我们要的数据
                            if (rxByteArrayTemp != null) {
                                rxByteArray = ArrayAppend(rxByteArrayTemp, null, 0);
                                rxByteArrayTemp = null;

                                String str = bytesToHexString(rxByteArray,rxByteArray.length);
                                Log.i(TAG,"按键的内容为："+str);
                                cb.TranToActivity(str);
                            }

                        }
                    } else {
                        ret = new byte[0];
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //return;
                }

                // 每100个毫秒去读取数据
                SystemClock.sleep(100);

            }
        }
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

    /**
     * 转换一个字节为十六进制
     * @param src 原来一个字节
     * @return 结果
     */
    private String byteToHexString(byte src) {

        String hex = Integer.toHexString(src & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }

        return hex.toUpperCase(Locale.US);
    }

    private int tranHex2ToInt(String hex){
        if(hex.length()!=2)
            return 0;

        return Integer.parseInt(hex,16);
    }

    private int asciitonum(String ascii){
        switch(ascii) {
            case "30":
                return 0;
            case "31":
                return 1;
            case "32":
                return 2;
            case "33":
                return 3;
            case "34":
                return 4;
            case "35":
                return 5;
            case "36":
                return 6;
            case "37":
                return 7;
            case "38":
                return 8;
            case "39":
                return 9;
            default:
                return 0;
        }
    }


}

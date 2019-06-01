package android_serialport_api;

import android.os.SystemClock;


import com.freshtribes.icecream.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ComLift {

    // 时分秒
    private static final SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    // 接线的串口线位置，默认是这个
    //private String portName = "/dev/ttyO7";
    private String portName = "/dev/ttyO2";

    // 串口对象
    private boolean isOpen = false;
    private SerialPort serialPort = null;

    // 输入与输出流
    private InputStream is = null;
    private OutputStream os = null;

    // 读取数据的线程
    private boolean readThreadisRunning = false;
    private ReadThread mReadThread;

    private static int snindex = 0;

    private byte[] rxByteArray = null;// 接收到的字节信息

    // 是否测试模式
    private boolean iTestMode = false;

    /**
     * 构造函数
     * @param portName 串口标记，如果是空，表示使用默认的串口
     */
    public ComLift(String portName){
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
                serialPort = new SerialPort(new File(portName), 9600, 0,1,SerialPort.PARITY_CHECK.NONE.ordinal());
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

        isOpen = false;
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
     * 售货函数
     *
     * @param hang  货道行坐标（1-9）
     * @param lie   货到列坐标(0-9)
     * @return 返回货道结构类型
     */
    public String[] vend_out_ind(int hang,int lie,int fen,String goodscode,String payway,String payinfo)
    {
        String rtn = "出货命令发送3次，对方无应答。";

        long nowTime = 0l;
        long endWhile = 0l;

        snindex++;
        if(snindex>250) snindex = 0;
        for(int i=1;i<=3;i++) {
            //ADD	CMD	    TYPE	LEN	    DAT	    CRC
            //1字节	1字节	1字节	1字节	数据域	2字节
            byte[] fa_cmd = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x01, (byte) 0x04,
                    (byte) 0x00, (byte) 0x01, (byte) 0x01,
                    (byte) 0x01, (byte) 0xAF};
            fa_cmd[4] = (byte) snindex;
            fa_cmd[5] = (byte) hang;
            fa_cmd[6] = (byte) lie;

            int crc = calcCrc16(fa_cmd);
            fa_cmd[7] = (byte) (crc % 256);
            fa_cmd[8] = (byte) (crc / 256);
            LogUtils.i("出货发送数据：" + bytesToHexString(fa_cmd,fa_cmd.length));

            rxByteArray = null;			// 接收到的字节信息

            long sendouttime = System.currentTimeMillis();
            try {
                if (os != null) {
                    os.write(fa_cmd, 0, fa_cmd.length);
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(iTestMode){
                // 测试用！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                SystemClock.sleep(3000);
                rxByteArray = new byte[]{ (byte) 0x00,(byte) 0x10,(byte) 0x01,(byte) 0x05,(byte) 0x05,(byte) 0x05,(byte) 0x05,(byte) 0x00,(byte) 0x05,(byte) 0xAC};
                // 测试用！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            }

            nowTime = System.currentTimeMillis();
            endWhile = nowTime + 30000;  // 最多等待30000毫秒=30秒
            while(nowTime < endWhile){
                SystemClock.sleep(10);
                if(rxByteArray == null){
                    // 需要再次发送,继续循环
                    nowTime = System.currentTimeMillis();
                    continue;  //while
                } else {
                    break;
                }
            }

            if(rxByteArray == null){
                continue;  //for
            }

            // 哈哈，有数据了
            LogUtils.i("出货接收到数据：" + bytesToHexString(rxByteArray,rxByteArray.length));

            if(rxByteArray.length == 5){
                int error = (int)rxByteArray[2];
                if(error == 1)
                    rtn =  "返回错误应答:非法功能";
                else if(error == 2)
                    rtn =  "返回错误应答:非法地址";
                else if(error == 3)
                    rtn =  "返回错误应答:非法数据";
                else
                    rtn =  "返回错误应答:未知";
                //break;

            } else if(rxByteArray.length == 10){
                int status = (int)rxByteArray[7];
                if(status == 0) {
                    long outoktime = System.currentTimeMillis();
                    long outusetime = outoktime - sendouttime;
                    if(outusetime < 2000){
                        LogUtils.i("出货正常完成了,花费时间（毫秒）："+outusetime+";snindex="+snindex+";因为小于2000，则序号加1后再次发送");
                        // 少于2秒的，应该是序号重复了，那么要再次发送
                        snindex++;
                        if(snindex>250) snindex = 0;
                        continue;
                    } else {
                        LogUtils.i("出货正常完成了,花费时间（毫秒）："+outusetime+";snindex="+snindex);

                        rtn = "";
                        break;
                    }
                } else {
                    if(status == 1)
                        rtn = "正确应答的命令状态字:系统正忙";
                    else if(status ==2)
                        rtn = "正确应答的命令状态字:货道故障";
                    else if(status ==3)
                        rtn = "正确应答的命令状态字:升降装置故障";
                    else if(status ==4)
                        rtn = "正确应答的命令状态字:移门装置故障";
                    else if(status ==5)
                        rtn = "正确应答的命令状态字:红外检测故障";
                    else if(status ==6)
                        rtn = "正确应答的命令状态字:取货门装置故障";
                    else
                        rtn = "正确应答的命令状态字:其它故障";

                    if(status != 1)
                        break;
                }
            } else {
                rtn = "收到应答的字节长度不对:"+rxByteArray.length;
                //break;
            }

        }
        return new String[]{rtn,""+hang+lie,""+fen,goodscode,payway,payinfo};
    }

    /**
     * 状态
     * @return 温度1,温度2,温度3,加热管1,加热管2,加热管3,加热管4,1号门开关,2号门开关,其它故障（有的话）,串口发送相关错误（有的话）
     */
    public String[] getStatus(){
        String[] rtn = new String[]{"-","-","-","0","0","0","0","-","-","","设备状态查询命令发送3次，对方无应答。"};
//        String[] rtn = new String[]{"温度传感器1的温度","温度传感器2的温度","温度传感器3的温度",
//                "加热管1是否正在加热","加热管2是否正在加热","加热管3是否正在加热","加热管4是否正在加热","1号门打开或关闭","2号门打开或关闭","","",
//                "升降电机位置传感器1故障","升降电机位置传感器2故障","升降电机位置传感器3故障","升降电机位置传感器4故障","升降电机位置传感器5故障","升降电机位置传感器6故障","升降电机位置传感器7故障","升降电机故障",
//                "移门电机或移门电磁铁故障","移门开关1故障","移门开关2故障","红外检测故障","取货口电磁铁故障","取货口开关故障","温度传感器1故障","温度传感器2故障",
//                "温度传感器3故障","","","","","","","",
//                "设备状态查询命令发送3次，对方无应答。"};// 最后一个是串口发送的信息

        long nowTime = 0l;
        long endWhile = 0l;

        for(int i=1;i<=3;i++) {
            //ADD	CMD	    TYPE	LEN	    DAT	    CRC
            //1字节	1字节	1字节	1字节	数据域	2字节
            byte[] fa_cmd = new byte[]{(byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x01,
                    (byte) 0x01, (byte) 0xAF};

            int crc = calcCrc16(fa_cmd);
            fa_cmd[4] = (byte) (crc % 256);
            fa_cmd[5] = (byte) (crc / 256);

            LogUtils.i("发送数据：" + bytesToHexString(fa_cmd,fa_cmd.length));
            rxByteArray = null;			// 接收到的字节信息

            try {
                if (os != null) {
                    os.write(fa_cmd, 0, fa_cmd.length);
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(iTestMode){
                // 测试用！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                SystemClock.sleep(300);
                rxByteArray = new byte[]{ (byte) 0x00,(byte) 0x10,(byte) 0x01,(byte) 0x05,
                        (byte) 99,(byte) 99,(byte) 99,
                        (byte) 0xFC,
                        (byte) 0x00,(byte) 0x00,(byte) 0x00,
                        (byte) 0x05,(byte) 0xAC};
                // 测试用！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            }

            nowTime = System.currentTimeMillis();
            endWhile = nowTime + 5000;  // 最多等待5000毫秒=5秒
            while(nowTime < endWhile){
                SystemClock.sleep(10);
                if(rxByteArray == null){
                    // 需要再次发送,继续循环
                    nowTime = System.currentTimeMillis();
                    continue;  //while
                } else {
                    break;
                }
            }

            if(rxByteArray == null){
                continue;  //for
            }

            // 哈哈，有数据了
            LogUtils.i("接收到数据：" + bytesToHexString(rxByteArray,rxByteArray.length));

            if(rxByteArray.length == 5){
                int error = (int)rxByteArray[2];
                if(error == 1)
                    rtn[rtn.length - 1] =  "返回错误应答:非法功能";
                else if(error == 2)
                    rtn[rtn.length - 1] =  "返回错误应答:非法地址";
                else if(error == 3)
                    rtn[rtn.length - 1] =  "返回错误应答:非法数据";
                else
                    rtn[rtn.length - 1] =  "返回错误应答:未知";
                continue;
            } else if(rxByteArray.length == 13){
                rtn[rtn.length - 1] = "";

                // 温度1
                int wendu1 = (int)(rxByteArray[4]);
                rtn[0] = "" + wendu1;
                // 温度2
                int wendu2 = (int)(rxByteArray[5]);
                rtn[1] = "" + wendu2;
                // 温度3
                int wendu3 = (int)(rxByteArray[6]);
                rtn[2] = "" + wendu3;

                // 控制状态字1
                byte control1 = rxByteArray[7];
                if((byte)(control1 & 0x80) == (byte)0x80){
                    rtn[3] = "1";// 加热管1正在加热
                } else {
                    rtn[3] = "0";// 加热管1停止加热
                }
                control1 = rxByteArray[7];
                if((byte)(control1 & 0x40) == (byte)0x40){
                    rtn[4] = "1";// 加热管2正在加热
                } else {
                    rtn[4] = "0";// 加热管2停止加热
                }
                control1 = rxByteArray[7];
                if((byte)(control1 & 0x20) == (byte)0x20){
                    rtn[5] = "1";// 加热管3正在加热
                } else {
                    rtn[5] = "0";// 加热管3停止加热
                }
                control1 = rxByteArray[7];
                if((byte)(control1 & 0x10) == (byte)0x10){
                    rtn[6] = "1";// 加热管4正在加热
                } else {
                    rtn[6] = "0";// 加热管4停止加热
                }

                control1 = rxByteArray[7];
                if((byte)(control1 & 0x08) == (byte)0x08){
                    rtn[7] = "1";// 1号门打开
                } else {
                    rtn[7] = "0";// 1号门关闭
                }
                control1 = rxByteArray[7];
                if((byte)(control1 & 0x04) == (byte)0x04){
                    rtn[8] = "1";// 2号门打开
                } else {
                    rtn[8] = "0";// 2号门关闭
                }

                String err = "";
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x80) == (byte)0x80){
                    err += "升降电机位置传感器1故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x40) == (byte)0x40){
                    err += "升降电机位置传感器2故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x20) == (byte)0x20){
                    err += "升降电机位置传感器3故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x10) == (byte)0x10){
                    err += "升降电机位置传感器4故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x08) == (byte)0x08){
                    err += "升降电机位置传感器5故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x04) == (byte)0x04){
                    err += "升降电机位置传感器6故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x02) == (byte)0x02){
                    err += "升降电机位置传感器7故障_";
                }
                control1 = rxByteArray[8];
                if((byte)(control1 & 0x01) == (byte)0x01){
                    err += "升降电机故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x80) == (byte)0x80){
                    err += "移门电机或移门电磁铁故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x40) == (byte)0x40){
                    err += "移门开关1故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x20) == (byte)0x20){
                    err += "移门开关2故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x10) == (byte)0x10){
                    err += "红外检测故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x08) == (byte)0x08){
                    err += "取货口电磁铁故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x04) == (byte)0x04){
                    err += "取货口开关故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x02) == (byte)0x02){
                    err += "温度传感器1故障_";
                }
                control1 = rxByteArray[9];
                if((byte)(control1 & 0x01) == (byte)0x01){
                    err += "温度传感器2故障_";
                }
                control1 = rxByteArray[10];
                if((byte)(control1 & 0x80) == (byte)0x80){
                    err += "温度传感器3故障_";
                }

                if(err.length()>0){
                    rtn[9] = err.substring(0,err.length()-2);
                }
                break;

            } else {
                rtn[rtn.length - 1] = "收到应答的字节长度不对:"+rxByteArray.length;
                continue;
            }

        }
        return rtn;
    }




    static byte[] crc16_tab_h = {
            (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
            (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01,
            (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
            (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
            (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01,
            (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40,
            (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
            (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01,
            (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
            (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
            (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01,
            (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41,
            (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81,
            (byte) 0x40 };

    static byte[] crc16_tab_l = {
            (byte) 0x00, (byte) 0xC0, (byte) 0xC1, (byte) 0x01, (byte) 0xC3, (byte) 0x03, (byte) 0x02, (byte) 0xC2, (byte) 0xC6, (byte) 0x06, (byte) 0x07, (byte) 0xC7, (byte) 0x05, (byte) 0xC5, (byte) 0xC4,
            (byte) 0x04, (byte) 0xCC, (byte) 0x0C, (byte) 0x0D, (byte) 0xCD, (byte) 0x0F, (byte) 0xCF, (byte) 0xCE, (byte) 0x0E, (byte) 0x0A, (byte) 0xCA, (byte) 0xCB, (byte) 0x0B, (byte) 0xC9, (byte) 0x09,
            (byte) 0x08, (byte) 0xC8, (byte) 0xD8, (byte) 0x18, (byte) 0x19, (byte) 0xD9, (byte) 0x1B, (byte) 0xDB, (byte) 0xDA, (byte) 0x1A, (byte) 0x1E, (byte) 0xDE, (byte) 0xDF, (byte) 0x1F, (byte) 0xDD,
            (byte) 0x1D, (byte) 0x1C, (byte) 0xDC, (byte) 0x14, (byte) 0xD4, (byte) 0xD5, (byte) 0x15, (byte) 0xD7, (byte) 0x17, (byte) 0x16, (byte) 0xD6, (byte) 0xD2, (byte) 0x12, (byte) 0x13, (byte) 0xD3,
            (byte) 0x11, (byte) 0xD1, (byte) 0xD0, (byte) 0x10, (byte) 0xF0, (byte) 0x30, (byte) 0x31, (byte) 0xF1, (byte) 0x33, (byte) 0xF3, (byte) 0xF2, (byte) 0x32, (byte) 0x36, (byte) 0xF6, (byte) 0xF7,
            (byte) 0x37, (byte) 0xF5, (byte) 0x35, (byte) 0x34, (byte) 0xF4, (byte) 0x3C, (byte) 0xFC, (byte) 0xFD, (byte) 0x3D, (byte) 0xFF, (byte) 0x3F, (byte) 0x3E, (byte) 0xFE, (byte) 0xFA, (byte) 0x3A,
            (byte) 0x3B, (byte) 0xFB, (byte) 0x39, (byte) 0xF9, (byte) 0xF8, (byte) 0x38, (byte) 0x28, (byte) 0xE8, (byte) 0xE9, (byte) 0x29, (byte) 0xEB, (byte) 0x2B, (byte) 0x2A, (byte) 0xEA, (byte) 0xEE,
            (byte) 0x2E, (byte) 0x2F, (byte) 0xEF, (byte) 0x2D, (byte) 0xED, (byte) 0xEC, (byte) 0x2C, (byte) 0xE4, (byte) 0x24, (byte) 0x25, (byte) 0xE5, (byte) 0x27, (byte) 0xE7, (byte) 0xE6, (byte) 0x26,
            (byte) 0x22, (byte) 0xE2, (byte) 0xE3, (byte) 0x23, (byte) 0xE1, (byte) 0x21, (byte) 0x20, (byte) 0xE0, (byte) 0xA0, (byte) 0x60, (byte) 0x61, (byte) 0xA1, (byte) 0x63, (byte) 0xA3, (byte) 0xA2,
            (byte) 0x62, (byte) 0x66, (byte) 0xA6, (byte) 0xA7, (byte) 0x67, (byte) 0xA5, (byte) 0x65, (byte) 0x64, (byte) 0xA4, (byte) 0x6C, (byte) 0xAC, (byte) 0xAD, (byte) 0x6D, (byte) 0xAF, (byte) 0x6F,
            (byte) 0x6E, (byte) 0xAE, (byte) 0xAA, (byte) 0x6A, (byte) 0x6B, (byte) 0xAB, (byte) 0x69, (byte) 0xA9, (byte) 0xA8, (byte) 0x68, (byte) 0x78, (byte) 0xB8, (byte) 0xB9, (byte) 0x79, (byte) 0xBB,
            (byte) 0x7B, (byte) 0x7A, (byte) 0xBA, (byte) 0xBE, (byte) 0x7E, (byte) 0x7F, (byte) 0xBF, (byte) 0x7D, (byte) 0xBD, (byte) 0xBC, (byte) 0x7C, (byte) 0xB4, (byte) 0x74, (byte) 0x75, (byte) 0xB5,
            (byte) 0x77, (byte) 0xB7, (byte) 0xB6, (byte) 0x76, (byte) 0x72, (byte) 0xB2, (byte) 0xB3, (byte) 0x73, (byte) 0xB1, (byte) 0x71, (byte) 0x70, (byte) 0xB0, (byte) 0x50, (byte) 0x90, (byte) 0x91,
            (byte) 0x51, (byte) 0x93, (byte) 0x53, (byte) 0x52, (byte) 0x92, (byte) 0x96, (byte) 0x56, (byte) 0x57, (byte) 0x97, (byte) 0x55, (byte) 0x95, (byte) 0x94, (byte) 0x54, (byte) 0x9C, (byte) 0x5C,
            (byte) 0x5D, (byte) 0x9D, (byte) 0x5F, (byte) 0x9F, (byte) 0x9E, (byte) 0x5E, (byte) 0x5A, (byte) 0x9A, (byte) 0x9B, (byte) 0x5B, (byte) 0x99, (byte) 0x59, (byte) 0x58, (byte) 0x98, (byte) 0x88,
            (byte) 0x48, (byte) 0x49, (byte) 0x89, (byte) 0x4B, (byte) 0x8B, (byte) 0x8A, (byte) 0x4A, (byte) 0x4E, (byte) 0x8E, (byte) 0x8F, (byte) 0x4F, (byte) 0x8D, (byte) 0x4D, (byte) 0x4C, (byte) 0x8C,
            (byte) 0x44, (byte) 0x84, (byte) 0x85, (byte) 0x45, (byte) 0x87, (byte) 0x47, (byte) 0x46, (byte) 0x86, (byte) 0x82, (byte) 0x42, (byte) 0x43, (byte) 0x83, (byte) 0x41, (byte) 0x81, (byte) 0x80,
            (byte) 0x40 };

    /**
     * 计算CRC16校验
     *
     * @param data
     *            需要计算的数组
     * @return CRC16校验值
     */
    public static int calcCrc16(byte[] data) {
        return calcCrc16(data, 0, data.length-2);
    }

    /**
     * 计算CRC16校验
     *
     * @param data
     *            需要计算的数组
     * @param offset
     *            起始位置
     * @param len
     *            长度
     * @return CRC16校验值
     */
    public static int calcCrc16(byte[] data, int offset, int len) {
        return calcCrc16(data, offset, len, 0xffff);
    }

    /**
     * 计算CRC16校验
     *
     * @param data
     *            需要计算的数组
     * @param offset
     *            起始位置
     * @param len
     *            长度
     * @param preval
     *            之前的校验值
     * @return CRC16校验值
     */
    public static int calcCrc16(byte[] data, int offset, int len, int preval) {
        int ucCRCHi = (preval & 0xff00) >> 8;
        int ucCRCLo = preval & 0x00ff;
        int iIndex;
        for (int i = 0; i < len; ++i) {
            iIndex = (ucCRCLo ^ data[offset + i]) & 0x00ff;
            ucCRCLo = ucCRCHi ^ crc16_tab_h[iIndex];
            ucCRCHi = crc16_tab_l[iIndex];
        }
        return ((ucCRCHi & 0x00ff) << 8) | (ucCRCLo & 0x00ff) & 0xffff;
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

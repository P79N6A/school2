package android_serialport_api;

import android.util.Log;

import com.mnevent.hz.bean.ComBean;
import com.mnevent.hz.Utils.MyFunc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


/**
 * @author benjaminwan ���ڸ���������
 */
public abstract class SerialHelper {
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SendThread mSendThread;
	private String sPort = "/dev/s3c2410_serial0";
	private int iBaudRate = 9600;
	private boolean _isOpen = false;
	private byte[] _bLoopData = new byte[] { 0x30 };
	private int iDelay = 500;
	// 数据位
	private int databits = 8;
	// 停止位
	private int stopbits = 1;
	// 校验位
	private int jiaoyanbits = 'N';

	// ----------------------------------------------------
	public SerialHelper(String sPort, int iBaudRate) {
		this.sPort = sPort;
		this.iBaudRate = iBaudRate;
	}

	public SerialHelper() {
		this("/dev/s3c2410_serial0", 9600);
	}

	public SerialHelper(String sPort) {
		this(sPort, 9600);
	}

	public SerialHelper(String sPort, String sBaudRate) {
		this(sPort, Integer.parseInt(sBaudRate));
	}

	// ----------------------------------------------------
	public void open() throws SecurityException, IOException, InvalidParameterException {
		Log.d("zlc1",iBaudRate+";"+databits+";"+stopbits+";"+jiaoyanbits);
		mSerialPort = new SerialPort(new File(sPort), iBaudRate, 0, databits, stopbits, jiaoyanbits);
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		mReadThread = new ReadThread();
		mReadThread.start();
		mSendThread = new SendThread();
		mSendThread.setSuspendFlag();
		mSendThread.start();
		_isOpen = true;
	}

	// ----------------------------------------------------
	public void close() {
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen = false;
	}

	// ----------------------------------------------------
	public void send(byte[] bOutArray) {
		try {
			mOutputStream.write(bOutArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------
	public void sendHex(String sHex) {
		byte[] bOutArray = MyFunc.HexToByteArr(sHex);
		send(bOutArray);
	}

	// ----------------------------------------------------
	public void sendTxt(String sTxt) {
		byte[] bOutArray = sTxt.getBytes();
		send(bOutArray);
	}

	// ----------------------------------------------------
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				try {
					if (mInputStream == null)
						return;
					byte[] buffer = new byte[512];
					if(false) {//changhua modify
						int size = mInputStream.read(buffer);
						if (size > 0) {
							ComBean ComRecData = new ComBean(sPort, buffer, size);
							onDataReceived(ComRecData);
						}
						try {
							Thread.sleep(50);// ��ʱ50ms
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						if(mInputStream.available() > 0 == false){
							//no data
							try {
								Thread.sleep(50);// ��ʱ50ms
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}else{
							//have data
							try {
							Thread.sleep(100);//wait for all data ready
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							int size = mInputStream.read(buffer);
							if (size > 0) {
								ComBean ComRecData = new ComBean(sPort, buffer, size);
								onDataReceived(ComRecData);
							}
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	// ----------------------------------------------------
	private class SendThread extends Thread {
		public boolean suspendFlag = true;// �����̵߳�ִ��

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				synchronized (this) {
					while (suspendFlag) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				send(getbLoopData());
				try {
					Thread.sleep(iDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// �߳���ͣ
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}

		// �����߳�
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
	}

	// ----------------------------------------------------
	public int getBaudRate() {
		return iBaudRate;
	}

	public boolean setBaudRate(int iBaud) {
		if (_isOpen) {
			return false;
		} else {
			iBaudRate = iBaud;
			return true;
		}
	}

	public boolean setBaudRate(String sBaud) {
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}

	// ----------------------------------------------------
	public String getPort() {
		return sPort;
	}

	public boolean setPort(String sPort) {
		if (_isOpen) {
			return false;
		} else {
			this.sPort = sPort;
			return true;
		}
	}

	// ----------------------------------------------------
	public boolean isOpen() {
		return _isOpen;
	}

	// ----------------------------------------------------
	public byte[] getbLoopData() {
		return _bLoopData;
	}

	// ----------------------------------------------------
	public void setbLoopData(byte[] bLoopData) {
		this._bLoopData = bLoopData;
	}

	// ----------------------------------------------------
	public void setTxtLoopData(String sTxt) {
		this._bLoopData = sTxt.getBytes();
	}

	// ----------------------------------------------------
	public void setHexLoopData(String sHex) {
		this._bLoopData = MyFunc.HexToByteArr(sHex);
	}

	// ----------------------------------------------------
	public int getiDelay() {
		return iDelay;
	}

	// ----------------------------------------------------
	public void setiDelay(int iDelay) {
		this.iDelay = iDelay;
	}

	// ----------------------------------------------------
	public void startSend() {
		if (mSendThread != null) {
			mSendThread.setResume();
		}
	}

	// ----------------------------------------------------
	public void stopSend() {
		if (mSendThread != null) {
			mSendThread.setSuspendFlag();
		}
	}
	// ----------------------------------------------------

	// ----------------------------------------------------
	public void setDatabits(String s) {
		databits = Integer.parseInt(s.substring(0, 1));
	}

	public void setStopbits(String s) {
		stopbits = Integer.parseInt(s.substring(0, 1));
	}

	public void setJiaoyanbits(String s) {
		String s2 = s.substring(0, 1);
		if (s2.equals("N")) {
			jiaoyanbits = 'n';
		}else if(s2.equals("O")){
			jiaoyanbits = 'o';
		}else if(s2.equals("E")){
			jiaoyanbits = 'e';
		}else{
			jiaoyanbits = 'n';
		}
	}

	protected abstract void onDataReceived(ComBean ComRecData);
}
package com.xwr.functiontest;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xwr.mulkeyboard.HexUtil;
import com.xwr.mulkeyboard.usbapi.UDevice;
import com.xwr.mulkeyboard.usbapi.UsbApi;
import com.xwr.mulkeyboard.utils.UsbUtil;

import java.io.UnsupportedEncodingException;

import static com.xwr.mulkeyboard.HexUtil.bytesToHexString;
import static java.sql.DriverManager.println;

public class UIccActivity extends BaseActivity implements View.OnClickListener {
  Button mReaderInit;
  Button mPowerOn;
  Button mPowerOff;
  Button mReadWrite;
  TextView result;
  byte APU1[] = {0x00, (byte) 0xa4, 0x04, 0x00, 0x0f, 0x73, 0x78, 0x31, 0x2e, 0x73, 0x68, 0x2e, (byte) 0xc9, (byte) 0xe7, (byte) 0xbb, (byte) 0xe1, (byte) 0xb1, (byte) 0xa3, (byte) 0xd5, (byte)
    0xcf};//卡片初始化1
  byte APU2[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x05};//卡片初始化2
  byte APU5[] = {0x00, (byte) 0xa4, 0x00, 0x00, 0x02, (byte) 0xef, 0x06};//卡验证
  byte APU3[] = {0x00, (byte) 0xb2, 0x07, 0x04, 0x0b};//获取社保卡号
  byte APU4[] = {0x00, (byte) 0xb2, 0x02, 0x04, 0x20};//姓名
  byte APU6[] = {0x00, (byte) 0xb2, 0x01, 0x04, 0x14};//身份号

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_uicc);
    mReaderInit = findViewById(R.id.readerInit);
    mPowerOn = findViewById(R.id.powerOn);
    mPowerOff = findViewById(R.id.powerOff);
    mReadWrite = findViewById(R.id.readWrite);
    result = findViewById(R.id.result);
    mReaderInit.setOnClickListener(this);
    mPowerOn.setOnClickListener(this);
    mReadWrite.setOnClickListener(this);
    mPowerOff.setOnClickListener(this);
    result.setMovementMethod(ScrollingMovementMethod.getInstance());
  }

  @Override
  public void onClick(View v) {
    long ret;
    byte slot = 0x01;
    switch (v.getId()) {
      case R.id.readerInit:
        if (UDevice.mDeviceConnection != null) {
          ret = UsbApi.Reader_Init(UDevice.mDeviceConnection, UDevice.usbEpIn, UDevice.usbEpOut);
          result.append("\nread init=" + ret);
        } else {
          showTmsg("请设置权限");
          try {
            UsbUtil.getInstance(this).initUsbData(0xffff,0xffff);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        break;
      case R.id.powerOn:
        byte[] atr = new byte[64];
        ret = UsbApi.ICC_Reader_PowerOn(slot, atr);
        result.append("\npower on=" + ret);
        if (ret > 0) {
          String data = null;
          data = bytesToHexString(atr, (int) ret);
          result.append(";data=" + data);
        } else {
          String data = new String("上电失败");
          result.append("\n" + data + " ret:" + ret);
        }
        ret = UsbApi.ICC_Reader_GetStatus(slot);
        result.append("\nstatus=" + ret);
        byte[] devId = new byte[64];
        ret = UsbApi.ICC_Reader_GetDevID(devId);
        result.append("\nget devId=" + ret + ";devId=" + bytesToHexString(devId, (int) ret));
        break;
      case R.id.readWrite:
        byte[] apdu1 = new byte[64];
        byte[] apdu2 = new byte[64];
        byte[] apdu3 = new byte[64];
        byte[] apdu4 = new byte[64];
        byte[] apdu5 = new byte[64];
        ret = UsbApi.ICC_Reader_Application(slot, 20, APU1, apdu1);
        println("init1 card=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 7, APU2, apdu1);
        println("init2 card=" + HexUtil.bytesToHexString(apdu1, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 5, APU3, apdu2);
        Log.i("xwr", "card num hex:" + HexUtil.bytesToHexString(apdu2, (int) ret));
        byte[] data1 = new byte[9];
        System.arraycopy(apdu2, 2, data1, 0, 9);
        Log.i("xwr", "card num:" + new String(data1));
        ret = UsbApi.ICC_Reader_Application(slot, 7, APU5, apdu3);
        Log.i("xwr","card verify:" + HexUtil.bytesToHexString(apdu3, (int) ret));
        ret = UsbApi.ICC_Reader_Application(slot, 5, APU6, apdu5);
        Log.i("xwr","card id:" + HexUtil.bytesToHexString(apdu5, (int) ret));
        byte[] data2 = new byte[18];
        System.arraycopy(apdu5, 2, data2, 0, 18);
        if (ret > 0) {
          result.append("\ncard num:" + new String(data2));
        } else {
          String data = new String("获取卡号失败");
          result.append("\n" + data);
        }
        long ret3 = UsbApi.ICC_Reader_Application(slot, 5, APU4, apdu4);
        println("--->>>card name Hex:" + HexUtil.bytesToHexString(apdu4, (int) ret3));
        if (ret3 > 0) {
          try {
            String name = new String(apdu4, 2, (byte) 0x1E, "GBK").trim();
            result.append("\ncard name:" + name);
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        } else {
          String data = new String("获取持卡人失败");
          result.append("\n" + data);
        }

       /* byte[] cmd = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};
        byte[] apdu = new byte[64];
        ret = UsbApi.ICC_Reader_Application(slot, 5, cmd, apdu);
        result.append("\napplication=" + ret);
        if (ret > 0) {
          String data = null;
          data = bytesToHexString(apdu, (int) ret);
          result.append(";data=" + data);
        } else {
          String data = new String("取随机数失败");
          result.append("\n" + data);
        }*/
        break;
      case R.id.powerOff:
        ret = UsbApi.ICC_Reader_PowerOff(slot);
        result.append("\npowerOff=" + ret);
        result.append("\n------------------------");
        break;
    }
  }

  //文字提示方法
  private void showTmsg(String msg) {
    Toast.makeText(UIccActivity.this, msg, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    UDevice.mDeviceConnection.close();
    UDevice.mDeviceConnection = null;
//    UsbUtil.getInstance(this).usbDestroy();
  }
}

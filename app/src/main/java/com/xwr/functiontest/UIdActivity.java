package com.xwr.functiontest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xwr.mulkeyboard.HexUtil;
import com.xwr.mulkeyboard.usbapi.USBDevice;
import com.xwr.mulkeyboard.usbapi.UsbApi;
import com.xwr.mulkeyboard.utils.UsbUtil;

import java.io.UnsupportedEncodingException;


public class UIdActivity extends BaseActivity implements View.OnClickListener {
  private TextView tvOutput, tvLeft1, tvLeft2;
  private ImageView mIvTest;
  boolean isRoll = false;
  Context mContext = this;
  Button roll;
  int i = 0;
  private Handler handler = new Handler();
  private Runnable task = new Runnable() {
    public void run() {
      if (USBDevice.mDeviceConnection == null) {
        try {
          UsbUtil.getInstance(mContext).initUsbData();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      Log.d("xwr", Thread.currentThread().getName());
      handler.postDelayed(this, 1000);//设置延迟时间
      //需要执行的代码
      if (isRoll) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            long ret;
            byte[] cardInfo = new byte[1300];
            ret = UsbApi.Syn_Get_Card(cardInfo);
            tvOutput.append("第" + i + "次：" + "get card=" + ret);
            Log.d("data", "data=" + HexUtil.bytesToHexString(cardInfo, 1300));
            tvOutput.append("\n----------------\n");
            i++;
          }
        });
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_uid);
    ((Button) findViewById(R.id.btnleft01)).setOnClickListener(this);
    roll = ((Button) findViewById(R.id.btnleft02));
    roll.setOnClickListener(this);
    findViewById(R.id.btnleft03).setOnClickListener(this);
    tvOutput = (TextView) findViewById(R.id.tvOutput);
    tvOutput.setMovementMethod(ScrollingMovementMethod.getInstance());
  }


  @Override
  public void onClick(View v) {
    long ret;
    switch (v.getId()) {
      case R.id.btnleft01:
        if (USBDevice.mDeviceConnection == null) {
          try {
            UsbUtil.getInstance(this).initUsbData();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        ret = UsbApi.Reader_Init(USBDevice.mDeviceConnection, USBDevice.usbEpIn, USBDevice.usbEpOut);
        tvOutput.append("\nread init=" + ret);
        byte[] cardInfo = new byte[1300];
        ret = UsbApi.Reader_Init(USBDevice.mDeviceConnection, USBDevice.usbEpIn, USBDevice.usbEpOut);
        tvOutput.append("\nread init=" + ret);
        ret = UsbApi.Syn_Get_Card(cardInfo);
        tvOutput.append("\nget card=" + ret);
        tvOutput.append("\ndata=" + HexUtil.bytesToHexString(cardInfo, 1300));
        if (ret == 0) {
          byte[] name = new byte[30];
          byte[] idnum = new byte[36];
          System.arraycopy(cardInfo, 0, name, 0, 30);
          System.arraycopy(cardInfo, 122, idnum, 0, 36);
          String cardName = null;
          String cardId = null;
          try {
            cardName = getString(name).trim();
            cardId = getString(idnum);
            tvOutput.append("\nget card=" + cardName + cardId);
            System.out.println(cardId);
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }

        }

        tvOutput.append("\n------------------\n");
        break;
      case R.id.btnleft02:
        isRoll = true;
        handler.post(task);
        roll.setClickable(false);
        break;
      case R.id.btnleft03:
        isRoll = false;
        roll.setClickable(true);
        break;
    }

  }

  private String getString(byte[] bytes) throws UnsupportedEncodingException {
    if (bytes != null && bytes.length > 0) {

      return new String(bytes, "UTF-16LE");
    }
    return "";
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }


  public void tvAppend(String data) {
    tvOutput.append(data);
    int offset = tvOutput.getLineCount() * tvOutput.getLineHeight();
    if (offset > tvOutput.getHeight()) {
      tvOutput.scrollTo(0, offset - tvOutput.getHeight());
    }
  }


}

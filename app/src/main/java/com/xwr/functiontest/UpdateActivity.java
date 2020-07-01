package com.xwr.functiontest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xwr.mulkeyboard.usbapi.UDevice;
import com.xwr.mulkeyboard.usbapi.UsbApi;
import com.xwr.mulkeyboard.utils.UsbUtil;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {
  private Button update;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_update);
    initData();
  }

  private void initData() {
    update = findViewById(R.id.update);
    update.setOnClickListener(this);
  }


  //文字提示方法
  private void showTmsg(String msg) {
    Toast.makeText(UpdateActivity.this, msg, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.update:
        long ret;
        if (UDevice.mDeviceConnection != null) {
          ret = UsbApi.Reader_Init(UDevice.mDeviceConnection, UDevice.usbEpIn, UDevice.usbEpOut);
          Log.d("xwr", "read init=" + ret);
          UsbApi.Reader_UpgraderMode();
        } else {
          showTmsg("请设置权限");
          try {
            UsbUtil.getInstance(this).initUsbData(0xffff, 0xffff);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (UDevice.mDeviceConnection != null) {
      UDevice.mDeviceConnection.close();
      UDevice.mDeviceConnection = null;
    }
    //    UsbUtil.getInstance(this).usbDestroy();
  }

}

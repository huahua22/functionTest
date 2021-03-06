package com.xwr.mulkeyboard;

import com.xwr.mulkeyboard.usbapi.UDevice;

import java.util.Arrays;

import static com.xwr.mulkeyboard.UKeyApi.key_clear;

/**
 * Create by xwr on 2020/4/28
 * Describe:
 */
public class KeyboardServer {

  public void key_init() {
    UKeyApi.key_reader_Init(UDevice.mDeviceConnection, UDevice.usbEpIn, UDevice.usbEpOut);
  }

  public int key_read_init() {
    byte[] scmd = {0x0a, (byte) 0xfe, 0x02, 0x73, 0x6f, 0x6c, 0x69, 0x63, 0x33, 0x03};
    byte[] response = new byte[1024];
    byte[] data = new byte[10];
    long ret;
    ret = UKeyApi.key_usb_init(response);
    System.arraycopy(response, 0, data, 0, 10);
    if (Arrays.equals(data, scmd)) {
      return 0;
    }
    return -1;
  }

  public String read_pwd() {
    byte[] response = new byte[1024];
    int ret, length;
    ret = UKeyApi.key_read_psd(response);
    if (ret > 0) {
      length = Integer.valueOf(response[0]) - 3;
      if (length > 0) {
        byte[] data = new byte[length];
        System.arraycopy(response, 3, data, 0, length);
        return new String(data);
      }
    }
    return null;
  }

  public String read_scan() {
    String result = null;
    byte[] response = new byte[1024];
    int ret = UKeyApi.key_scan(response);
    System.out.println("scan:" + HexUtil.bytesToHexString(response, ret));
    if (ret > 0) {
      int length;
      length = (response[0] & 0xFF) - 4;
      if (length > 0) {
        byte[] data = new byte[length];
        System.arraycopy(response, 4, data, 0, length);
        System.out.println(HexUtil.bytesToHexString(data, length));
        result = HexUtil.base64Decode(new String(data), "");
        return result;
      }
    }
    return result;
  }

  public String read_old_pwd() {
    byte[] response = new byte[1024];
    int ret, length;
    ret = UKeyApi.key_read_old_psd(response);
    if (ret > 0) {
      length = Integer.valueOf(response[0]) - 3;
      if (length > 0) {
        byte[] data = new byte[length];
        System.arraycopy(response, 3, data, 0, length);
        return new String(data);
      }
    }
    return null;
  }

  public String read_new_pwd() {
    byte[] response = new byte[1024];
    int ret, length;
    ret = UKeyApi.key_read_new_psd(response);
    if (ret > 0) {
      length = Integer.valueOf(response[0]) - 3;
      if (length > 0) {
        byte[] data = new byte[length];
        System.arraycopy(response, 3, data, 0, length);
        return new String(data);
      }
    }
    return null;
  }

  public String read_new_pwd_again() {
    byte[] response = new byte[1024];
    int ret, length;
    ret = UKeyApi.key_read_new_psd_again(response);
    if (ret > 0) {
      length = Integer.valueOf(response[0]) - 3;
      if (length > 0) {
        byte[] data = new byte[length];
        System.arraycopy(response, 3, data, 0, length);
        return new String(data);
      }
    }
    return null;
  }

  public int read_clear() {
    return key_clear();
  }


  public String read_serialnumber() {
    byte[] response = new byte[1024];
    int ret, length;
    ret = UKeyApi.key_read_serialnumber(response);
    if (ret > 0) {
      length = Integer.valueOf(response[0]) - 3;
      byte[] data = new byte[length];
      System.arraycopy(response, 3, data, 0, length);
      return new String(data);
    }
    return null;
  }
}

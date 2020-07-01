package com.xwr.functiontest;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
  Camera mCamera;
  SurfaceView mSurfaceView;
  SurfaceHolder mSurfaceHolder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    mSurfaceView = findViewById(R.id.sv_cam);
    initView();
  }

  private void initView() {
    mSurfaceHolder = mSurfaceView.getHolder();
    mSurfaceHolder.addCallback(this);

  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (mCamera != null) {
      mCamera.startPreview();
    } else {
      mCamera = Camera.open(0);
      try {
        mCamera.setPreviewDisplay(mSurfaceHolder);
      } catch (IOException e) {
        e.printStackTrace();
      }
      ;
      mCamera.startPreview();
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    if (mCamera != null) {
      mCamera.setPreviewCallback(null);
      mCamera.release();
    }
    mCamera = null;
  }
}

package com.xwr.functiontest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void mulkey(View view) {
    startActivity(new Intent(this, MulkeyActivity.class));
  }

  public void readerIc(View view) {
    startActivity(new Intent(this, UIccActivity.class));
  }

  public void readerId(View view) {
    startActivity(new Intent(this, UIdActivity.class));
  }

}

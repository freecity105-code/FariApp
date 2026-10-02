package com.fari.maktabeh;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("مکتبه کامله فارسی\nنسخه اولیه");
        view.setTextSize(24);
        view.setTextDirection(TextView.TEXT_DIRECTION_RTL);
        view.setPadding(32, 32, 32, 32);
        setContentView(view);
    }
}

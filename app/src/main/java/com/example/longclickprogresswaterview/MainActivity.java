package com.example.longclickprogresswaterview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = findViewById(R.id.tv_progress);

        WaterProgressView longClickProgressView = findViewById(R.id.btn_long_click_finish);
        longClickProgressView.setBgColor(Color.parseColor("#000000"));  //设置背景颜色
        longClickProgressView.setProgressColor(Color.parseColor("#FFFFFF"));  //设置进度(水漫)颜色
        //设置进度监听回调
        longClickProgressView.setOnLongClickStateListener(new WaterProgressView.OnLongClickStateListener() {
            @Override
            public void onFinish() {
                //当完成读条时执行
                Toast.makeText(MainActivity.this, "Finish!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(float progress) {
                //进度条改变时执行
                textView.setText(progress + "%");
            }

            @Override
            public void onCancel() {
                //取消长按时执行
                Toast.makeText(MainActivity.this, "Cancel!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
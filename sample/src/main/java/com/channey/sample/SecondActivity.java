package com.channey.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.channey.timerbutton.TimerButton;

public class SecondActivity extends AppCompatActivity {
    private TimerButton timerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        timerButton = (TimerButton) findViewById(R.id.tv2);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerButton.start();
                //do something like request api
            }
        });

        timerButton.setOnCountingListener(new TimerButton.CountingListener() {
            @Override
            public void onCounting(long time) {
                timerButton.setText(time / 1000 + "s");
            }

            @Override
            public void onFinished() {
                timerButton.setText("click");
            }
        });
    }
}

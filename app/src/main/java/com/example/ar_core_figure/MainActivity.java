package com.example.ar_core_figure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    myGLSirfaceView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myView = new myGLSirfaceView(this);
        setContentView(myView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        myView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        myView.onResume();
    }
}
package com.example.ar_core_figure;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class myGLSirfaceView extends GLSurfaceView {
    public myGLSirfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        setRenderer(new MyGLRenderer());

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public myGLSirfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}

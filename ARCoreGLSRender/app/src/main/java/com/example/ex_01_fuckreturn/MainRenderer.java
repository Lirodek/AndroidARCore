package com.example.ex_01_fuckreturn;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    RenderCallBack myCallBack;

    interface RenderCallBack{
        void preRender();   //MainActivity 에서 재정의하여 호출토록 함
    }

    //생성시 RenderCallBack을  매개변수로 대입받아 자신의 멤버로 넣는다
    // MainActivity 에서 생성하므로 MainAcitivity의 것을 받아서 처리가능 토록 한다.
    MainRenderer(RenderCallBack myCallBack){
        this.myCallBack = myCallBack;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d("MainRenderer : ", "onSurfaceCreated() 실행");

                            
                            // R       G          B          A --> 노랑색
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.d("MainRenderer : ", "onSurfaceChanged() 실행");

        GLES20.glViewport(0,0,width,height);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.d("MainRenderer : ", "onDrawFrame() 실행");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    int getTextureId(){
        return 0;
    }
}

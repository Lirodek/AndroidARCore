package com.example.ar_core_map;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.google.ar.core.Session;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    ObjRenderer mObj;
    ArrayList<Cube> mCubes = new ArrayList<>();

    boolean mViewport;
    int mViewportWidth, mViewportHeight;

    RenderCallBack mRenderCallBack;

    float[] mProjMatrix = new float[16];

    void addCube(MyPlace place){
        //큐브생성
        Cube box = new Cube(0.03f, place.color, 0.8f);
        //Sphere box = new Sphere(0.01f, place.color);
        float[] matrix = new float[16];

        //좌표 초기화
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix,0, (float)place.arPos[0], (float)place.arPos[1], (float)place.arPos[2]);

        box.setProjectionMatrix(mProjMatrix);
        // mRenderer.mCube.setViewMatrix(viewMatrix);
        box.setModelMatrix(matrix);
        //박스추가
        mCubes.add(box);
    }

    MainRenderer(RenderCallBack callBack, Context context) {
        mRenderCallBack = callBack;
        mCamera = new CameraPreView();

        mObj = new ObjRenderer(context, "madara.obj", "madara.png");
    }

    interface RenderCallBack {
        void preRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();
        mObj.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewport = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderCallBack.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);

        for(Cube box : mCubes){
            if(!box.isInited){
                box.init();
            }
            box.draw();
        }
        mObj.draw();
    }

    void updateSession(Session session, int displayRotation) {
        if (mViewport) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewport = false;
        }
    }

    public void setPorojectMatrix(float[] matrix) {
        System.arraycopy(matrix ,0, mProjMatrix, 0, 16);
        mObj.setProjectionMatrix(matrix);

    }

    public void updateViewMatrix(float[] matrix) {


        for(Cube box: mCubes){
            box.setViewMatrix(matrix);
        }
        mObj.setViewMatrix(matrix);

    }

    public int getTextureId() {
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }
}

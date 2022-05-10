package com.example.ar_core_transform;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    PointCloudRenderer mPointCloud;
    PlaneRenderer mPlane;
    Context context;
    ObjRenderer mObj;
    ObjRenderer mObj2;
    String[][] object = {{"andy.obj","andy.png"},{"madara.obj", "madara.png"}};

    boolean mViewport;
    int mViewportWidth, mViewportHeight;
    RenderCallBack mRenderCallBack;
    MainRenderer(RenderCallBack callBack, Context context){
        mRenderCallBack=callBack;
        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();
        mPlane = new PlaneRenderer(Color.BLUE, 0.7f);

        mObj = new ObjRenderer(context,"madara.obj", "madara.png");
        mObj2 = new ObjRenderer(context, "andy.obj", "andy.png");
        this.context = context;

    }

    interface RenderCallBack{
        void preRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f,1.0f, 0.0f, 1.0f);

        mCamera.init();
        mPointCloud.init();
        mPlane.init();

        mObj.init();
        mObj2.init();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0,width, height);
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

        mPointCloud.draw();
        mPlane.draw();
        mObj.draw();
        mObj2.draw();

    }

    void updateSession(Session session, int displayRotation){
        if(mViewport){
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewport = false;
        }
    }

    public void setPorojectMatrix(float[] matrix){
        mPointCloud.updateProjMatrix(matrix);
        mPlane.setProjectionMatrix(matrix);
        mObj.setProjectionMatrix(matrix);
        mObj2.setProjectionMatrix(matrix);

    }

    public void updateViewMatrix(float[] matrix){
        mPointCloud.updateViewMatrix(matrix);
        mPlane.setViewMatrix(matrix);
        mObj.setViewMatrix(matrix);
        mObj2.setProjectionMatrix(matrix);

    }

    public int getTextureId(){
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }
}

package com.example.ar_core_catch;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.Session;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    PointCloudRenderer mPointCloud;
    PlaneRenderer mPlane;

    ArrayList<ObjRenderer> objs = new ArrayList<>();

    boolean mViewport;
    int mViewportWidth, mViewportHeight;
    RenderCallBack mRenderCallBack;
    MainRenderer(RenderCallBack callBack, Context context){
        mRenderCallBack=callBack;
        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();
        mPlane = new PlaneRenderer(Color.BLUE, 0.7f);
        objs.add(new ObjRenderer(context, "andy.obj", "andy.png"));
        objs.add(new ObjRenderer(context, "notebook.obj", "notebook.jpg"));
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
        for(int i=0; i<objs.size();i++){
            objs.get(i).init();
        }
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

        for(int i=0; i<objs.size();i++){
            objs.get(i).draw();
        }
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
        for(int i=0; i<objs.size();i++){
            objs.get(i).setProjectionMatrix(matrix);
        }

    }

    public void updateViewMatrix(float[] matrix){
        mPointCloud.updateViewMatrix(matrix);
        mPlane.setViewMatrix(matrix);

        for(int i=0; i<objs.size();i++){
            objs.get(i).setViewMatrix(matrix);
        }

    }

    public int getTextureId(){
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }
}

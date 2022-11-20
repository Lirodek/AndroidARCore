package com.example.ar_core_transform;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 이동, 회전 이벤트 처리할 객체
    GestureDetector mGestureDetector;
    // 크기조절 이벤트 처리할 객체
    ScaleGestureDetector scaleGestureDetector;


    GLSurfaceView mSurfaceView;
    MainRenderer mainRenderer;
    TextView myTextView;
    Button changeBtn;

    Session session;
    Config mConfig;

    DisplayListener displayListener = new DisplayListener();
    CallBack callBack = new CallBack();
    Gesture simpleGesture = new Gesture();
    ScaleGes simpleScaleGesture = new ScaleGes();

    float[] modelMatrix = new float[16];
    float mCurrentX, mCurrentY, mRotateFactor=0f, mScaleFactor = 1f;
    boolean userRequestedInstall = true, mTouched = false, isModelInit = false, objChange = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noTitleBar();
        setContentView(R.layout.activity_main);

        myTextView = (TextView)findViewById(R.id.textView);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        changeBtn = (Button) findViewById(R.id.change);

        // 제스처 이밴트 콜백 함수 객체를 생성자 매개변수로 처리 (이벤트 핸들러)
        mGestureDetector = new GestureDetector(this, simpleGesture);
        scaleGestureDetector = new ScaleGestureDetector(this, simpleScaleGesture);
        changeBtn.setOnClickListener(new Click());

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if(displayManager != null)
            displayManager.registerDisplayListener(displayListener, null);

        mainRenderer = new MainRenderer(callBack, this);
        settingSurfaceView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        session.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requstCameraPermission();
        try {
            if (session == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, userRequestedInstall)) {
                    case INSTALLED:
                        session = new Session(this);
                        Log.d("메인", "ARCroe Session get도다제");
                        break;
                    default:
                        Log.d("메인", "AR Core의 설치가 필요해");
                        userRequestedInstall = false;
                        break;
                }
            }
        } catch (Exception e) {
        }


        mConfig = new Config(session);
        session.configure(mConfig);
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    class CallBack implements MainRenderer.RenderCallBack{

        @Override
        public void preRender() {
            if(mainRenderer.mViewport){
                Display display = getWindowManager().getDefaultDisplay();
                int displayRotation = display.getRotation();
                mainRenderer.updateSession(session, displayRotation); // 화면회전된걸 보고 돕니당
            }
            session.setCameraTextureName(mainRenderer.getTextureId());
            Frame frame = null;

            try {
                frame = session.update();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
            }
            if(frame.hasDisplayGeometryChanged())
                mainRenderer.mCamera.transformDisplayGeometry(frame);

            PointCloud pointCloud = frame.acquirePointCloud();
            mainRenderer.mPointCloud.update(pointCloud);
            pointCloud.release();

            if(mTouched){
                List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                for(HitResult result : results){
                    Pose pose = result.getHitPose(); // 증강공간에서의 좌표
                    if(!isModelInit){
                        isModelInit = true;
                        pose.toMatrix(modelMatrix, 0);
                        Matrix.rotateM(modelMatrix, 0, mRotateFactor, 0f, 1f, 0f);
                        Matrix.scaleM(modelMatrix, 0, mScaleFactor, mScaleFactor, mScaleFactor);
                    }
                    // 증강공간에서의 좌표에 객체가 있는지 받아온다.
                    Trackable trackable = result.getTrackable();
                    if(trackable instanceof Plane &&((Plane)trackable).isPoseInPolygon(pose)
                    ) {
                        if(objChange)
                            mainRenderer.mObj.setModelMatrix(modelMatrix);
                        else {
                            mainRenderer.mObj2.setModelMatrix(modelMatrix);
                        }
                    }
                }
            }

            // Session으로부터 증강현실 속에서의 평면이나, 점 객체를 얻을 수 있다.
            //                              Plane       Point
            Collection<Plane> planes = session.getAllTrackables(Plane.class);

            boolean isPlaneDatected = false;

            for(Plane plane : planes){
                if(plane.getTrackingState() == TrackingState.TRACKING && plane.getSubsumedBy() == null){
                    mainRenderer.mPlane.update(plane); // 랜더링에서 plane 정보를 갱신하여 출력
                    isPlaneDatected = true;
                }

            }
            if(isPlaneDatected){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myTextView.setText("평면 찾았습니다.");
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myTextView.setText("평면 못 찾았습니다.");
                    }
                });
            }

            Camera camera = frame.getCamera();
            float[] projMatrix = new float[16];
            camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);
            float[] viewMatrix = new float[16];
            camera.getViewMatrix(viewMatrix, 0);

            mainRenderer.setPorojectMatrix(projMatrix);
            mainRenderer.updateViewMatrix(viewMatrix);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
    class Gesture extends GestureDetector.SimpleOnGestureListener{
        // 따닥 처리 (이동)
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mTouched = true;
            isModelInit = false;
            mCurrentX = e.getX();
            mCurrentY = e.getY();
            Log.d("DoubleClick",e.getX()+", "+e.getY());
            return true;
        }
        // 드래그 처리(회전)
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if(isModelInit){
                Log.d("Scroll",distanceX+", "+distanceY);
                // 변화량을 누적하여 초기화시 변화된 총량을 회전값으로 넣겠다.
                mRotateFactor += -distanceX/5;

                // 지금의 변화량만 넣겠다
                // 회전( 각도 )
                Matrix.rotateM(modelMatrix, 0, -distanceX/5, 0f, 1f, 0f);
            }

            return true;
        }
    }

    class ScaleGes extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // 두 손가락으로 스케일 시 작동
            if(isModelInit){
                mScaleFactor *= detector.getScaleFactor();
                Matrix.scaleM(modelMatrix, 0 ,detector.getScaleFactor(), detector.getScaleFactor(), detector.getScaleFactor());

            }

            return true;
        }
    }
    class Click implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(objChange){
                objChange = false;
            } else {
                objChange = true;
            }

        }
    }

    class DisplayListener implements DisplayManager.DisplayListener{
        @Override
        public void onDisplayAdded(int i) { }
        @Override
        public void onDisplayRemoved(int i) {  }
        @Override
        public void onDisplayChanged(int i) {
            synchronized (this){
                mainRenderer.mViewport = true;
            }
        }
    }

    private void settingSurfaceView(){
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mSurfaceView.setRenderer(mainRenderer);
    }

    private void noTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void requstCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
    }
}
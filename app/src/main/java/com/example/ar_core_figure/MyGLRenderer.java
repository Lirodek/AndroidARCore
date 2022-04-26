package com.example.ar_core_figure;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    Square myBox;
    Square myBox1;
    Square myBox2;
    Square myBox3;


    float[] mMatrix = new float[16];
    float[] mProjectionMatrix = new float[16];
    float[] mViewMatrix = new float[16];





    static float squarCoords[] = {
            //  x,   y,   z
            -0.5f,-0.5f,-0.5f,    /* 뒤좌하*/

            -0.5f, 0.5f,-0.5f,    /* 뒤좌상 */

            0.5f, 0.5f,-0.5f,    /* 뒤우상 */

            0.5f,-0.5f,-0.5f,    /* 뒤우하 */

            -0.5f,-0.5f, 0.5f,    /* 앞좌하 */

            -0.5f, 0.5f, 0.5f,    /* 앞좌상 */

            0.5f, 0.5f, 0.5f,    /* 앞우상 */

            0.5f,-0.5f, 0.5f };
    float[] color = { 0.0f, 1.f, 0.3f, 1.0f };

    // 그리는 순서

    short[] drawOrder = { 0,1,2, 0,2,3,
            4,6,5, 4,7,6,    /* 앞면 */

            0,4,5, 0,5,1,    /* 좌면 */

            1,5,6, 1,6,2,    /* 윗면 */

            2,6,7, 2,7,3,    /* 우면 */

            3,7,4, 3,4,0 };

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);

        myBox = new Square();
    }
    // 화면갱신 되면서 화면 에서 배치치
   @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        float ratio = (float) width / height;
        // 아래의 frustumM을 사용하는 이유는
        // 휴대폰 마다 넓이가 다르기 때문에 사용해줍니다.
        Matrix.frustumM(mProjectionMatrix, 0, // float[] 를 넣고 시작번지를, 0 으로 줍니다
               -ratio, ratio, //
               -1,1,3,7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0,// 배열, 배열의 시작 번지수
                // x, y, z
                0, 2,-3 , // 카메라의 위치
                0, 0, 0, // 카메라 시선
                5, 1, 5 // 카메라 윗방향
                );

        // 매트릭스를 담을 배열 선정 0번지에서부터
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // 정사각형 그리기기
       myBox.draw(mMatrix);

    }


    // GPU를 이용하여 그리기를 연산한다.
    static int loadShader(int type, String shaderCode){

        int res = GLES20.glCreateShader(type);

        GLES20.glShaderSource(res, shaderCode);
        GLES20.glCompileShader(res);

        return res;
    }
}

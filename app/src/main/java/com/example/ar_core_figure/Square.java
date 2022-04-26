package com.example.ar_core_figure;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {
    String vertexShaderCode = "uniform mat4 uMVPMatrix;" +  // 4 x 4 형태의 상수로 지정
                              "attribute vec4 vPosition;" +  // vec4 -> 3차원 좌표
                              "void main(){" +
                                    "gl_Position = uMVPMatrix * vPosition ;" +
                                        //gl_Position 은 OpenGL 에 있는 변수 계산식 uMVPMatrix * vPosition
                              "}";

    String fragmentShaderCode = "precision mediump float;" +
                                // 정밀도는 중간으로 맞춰줍니다.
                                "uniform vec4 vColor;" +
                                "void main(){" +
                                    "gl_FragColor = vColor; " +
                                    // color를 지정해줍니다.
                                "}";

    // 직사각형 점의 좌표
//    static float squarCoords[] = {
//            //  x,   y,   z
//            -0.5f, -0.5f, 1.0f,    // 왼쪽 위
//            -0.5f, 0.5f, 1.0f,   // 왼쪽 아래
//            0.5f,  0.5f, 1.0f,   // 오른쪽 아래
//            0.5f,  -0.5f, 1.0f,     // 오른쪽 위
//    };

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

    short[] drawOrder = {
            0,1,2, 0,2,3,
            4,6,5, 4,7,6,    /* 앞면 */
            0,4,5, 0,5,1,    /* 좌면 */
            1,5,6, 1,6,2,    /* 윗면 */
            2,6,7, 2,7,3,    /* 우면 */
            3,7,4, 3,4,0 };



    float[] mMatrix;
    int mProgram;
    FloatBuffer vertexBuffer;
    ShortBuffer drawBuffer;

    public Square(){

        ByteBuffer bb = ByteBuffer.allocateDirect( squarCoords.length * 4 );
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squarCoords);
        vertexBuffer.position(0); // 내부 포지션을 조정해준다.

        bb = ByteBuffer.allocateDirect( drawOrder.length * 2 );
        bb.order(ByteOrder.nativeOrder());

        drawBuffer = bb.asShortBuffer();
        drawBuffer.put(drawOrder);
        drawBuffer.position(0); // 내부 포지션을 조정해준다.

        //
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode
        );
        // 점색상 계산
        // fragmentShaderCode -> fragmentSjader
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode
        );

        mProgram = GLES20.glCreateProgram();
        // 점위치 계산식 합치기
        GLES20.glAttachShader(mProgram, vertexShader);
        // 색상 계산식 합치기
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram); // 도형 정보 렌더를 계산합니다.
    }

    int mPositionHandle, mColorHandle, mMVPHandle;


    // 도형그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(float[] mMatrix){

        // 계산된 렌더링 정보 사용한다.
        GLES20.glUseProgram(mProgram);

        //            vPosition
        // mProgram ==> vertexShader
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(
                mPositionHandle,
                3,           // 점속성 - 좌표계
                GLES20.GL_FLOAT,  // 점의 자료형 float
                false,  // 정규화 = true, 직접변환 false
                3 * 4,      // 점속성의 stride(간격)
                vertexBuffer      // 점 정보
        );

        //            vPosition
        // mProgram ==> vertexShader
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mMVPHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // 그려지는 곳에 위치, 보이는 정보를 적용한다.
        GLES20.glUniformMatrix4fv(mMVPHandle, 1, false, mMatrix, 0);

        // 직사각형을 그린다.
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT,
                drawBuffer
        );
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

package com.example.arcore_motiontracking;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.ar.core.PointCloud;

public class PointCloudRenderer {

    float[] mViewMatrix = new float[16];
    float[] mProjMatrix = new float[16];



    // GPU 를 이용하여 고속 계산 하여 화면 처리 하기 위한 코드
    String vertexShaderCode =
            "uniform mat4 uMvpMatrix ; " +
            "uniform vec4 uColor ; " +
            "uniform float uPointSize ; " +
            "attribute vec4 vPosition;" +    // vec4 -> 3차원 좌표"uniform vec2 vColor;" +
            "varying vec4 vColor;" +

                    "void main () {" +
                    "vColor = uColor ;" +
                    "gl_Position = uMvpMatrix * vec4( vPosition.xyz , 1.0 ) ;" +
                    // gl_Position  " OpenGL 에 있는 변수 ::> 계산식 uMVPMatrix * vPosition
                    "gl_PointSize = uPointSize ;" +
                    "}";

    String fragmentShaderCode =
            // 정밀도는 중간으로 맞춰줍니다.
            "precision mediump float; " +
                    "varying vec4 vColor; " +
                    "void main(){ " +
                    "    gl_FragColor = vColor; " +  // color를 지정해줍니다.
                    "}";


    // 직사각형 점의 좌표
//    static float [] QUARD_COORDS = {
//            // x,   y,   z
//            -1.0f, -1.0f, 0.0f,
//            -1.0f, 1.0f, 0.0f,
//            1.0f, -1.0f, 0.0f,
//            1.0f, 1.0f, 0.0f,
//
//    };
//
//    static float [] QUARD_TEXCOORDS = {
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//
//    };

//    FloatBuffer mVertices; // 점정보
//    FloatBuffer mTexCoords; // 텍스좌표
//    FloatBuffer mTextCoordsTransformed;

    int[] mVbo;

    int mProgram;
    int mNumPoints = 0;


    PointCloudRenderer(){

    }

    // 카메라 초기화
    void init(){
        mVbo= new int[1];

        GLES20.glGenBuffers(1, mVbo, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 1000 * 16, null, GLES20.GL_DYNAMIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // 점쉐이더 생성
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vertexShaderCode);


        // 컴파일
        GLES20.glCompileShader(vShader);

//        int[] compiled = new int[1];
//        GLES20.glGetShaderiv(vShader,GLES20.GL_COMPILE_STATUS,compiled,0);

        //텍스처
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fragmentShaderCode);

        // 컴파일
        GLES20.glCompileShader(fShader);
//        GLES20.glGetShaderiv(fShader,GLES20.GL_COMPILE_STATUS,compiled,0);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,vShader);
        GLES20.glAttachShader(mProgram,fShader);
        GLES20.glLinkProgram(mProgram);

    }

    // 3차원 좌표 값을 갱신할 메소드
    void update(PointCloud pointCloud){

        // 점 위치 정보 받기 위한 바인딩
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);

        // 그려야 할 점 갯수 갱신
        // 점 정보. 점들. 점들중앙 그리고 남아있는 정보 / 4;
        mNumPoints = pointCloud.getPoints().remaining() / 4;

        // 점 위치 정보 Buffer로 갱신
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mNumPoints * 16, pointCloud.getPoints());

        // 점 위치 정보 바인딩 해제
       GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    // 카메라로 그리기
    void draw(){

        float[] mMVPMatrix = new float[16];

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mProjMatrix, 0);

        // 계산된 렌더링 정보 사용한다.
        GLES20.glUseProgram(mProgram);

                 /*"uniform mat4 uMvpMatrix ; " +
                "uniform vec4 uColor ; " +
                "uniform float uPointSize ; " +
                "attribute vec4 vPosition;"*/
        int position = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int color = GLES20.glGetUniformLocation(mProgram, "uColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix");
        int size = GLES20.glGetUniformLocation(mProgram, "uPointSize");

        // GPU 활성화
        GLES20.glEnableVertexAttribArray(position);

        // 바인딩
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);


        // 점 포인터
        GLES20.glVertexAttribPointer(
                position,
                4,           // 점속성 - 좌표계
                GLES20.GL_FLOAT,  // 점의 자료형 float
                false,  // 정규화 = true, 직접변환 false
                16,      // 점속성의 stride(간격)
                0      // 점 정보
        );

        GLES20.glUniform4f(color, 1.0f, 0.0f, 0.0f, 1.0f);


        // 그려지는 곳에 위치, 보이는 정보를 적용한다.
        GLES20.glUniformMatrix4fv(mvp, 1, false, mMVPMatrix, 0);

        // 점 크기기
        GLES20.glUniform1f(size, 50.0f);

       // 직사각형을 그린다.
        GLES20.glDrawArrays(
                GLES20.GL_POINTS,
                0,
                mNumPoints // 점갯수
        );

        // 닫는다다
       GLES20.glDisableVertexAttribArray(position);

        // 바인딩
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    void updateMatrix(float[] mViewMatrix, float[] mProMatrix){
        // 배열 복제
        //              원본        시작위치       복사될배열, 복사배열 시작위치, 개수
       System.arraycopy(mViewMatrix, 0, this.mViewMatrix, 0,           16);

        System.arraycopy(mProMatrix, 0, this.mProjMatrix, 0,           16);

    }
}

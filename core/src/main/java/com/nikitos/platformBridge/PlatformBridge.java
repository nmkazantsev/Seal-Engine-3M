package com.nikitos.platformBridge;

/**
 * an abstract class to be implemented by platform - dependent implementations
 */
public abstract class PlatformBridge {
    public abstract void onPause();

    public abstract void onResume();

    public abstract void setLookAtM(float[] rm, int rmOffset,
                                    float eyeX, float eyeY, float eyeZ,
                                    float centerX, float centerY, float centerZ,
                                    float upX, float upY, float upZ);


    public abstract void orthoM(float[] m, int offset,
                                float left, float right, float bottom, float top,
                                float near, float far);

    public abstract void frustumM(float[] m, int offset,
                                  float left, float right, float bottom, float top,
                                  float near, float far);

    public abstract void multiplyMM(float[] result, int resultOffset,
                                    float[] lhs, int lhsOffset,
                                    float[] rhs, int rhsOffset);


    public abstract void translateM(float[] m, int mOffset,
                                    float x, float y, float z);


    public abstract void rotateM(float[] m, int mOffset,
                                 float a, float x, float y, float z);


    public abstract void scaleM(float[] m, int mOffset,
                                float x, float y, float z);


    public abstract void setIdentityM(float[] sm, int smOffset);


    public abstract boolean invertM(float[] mInv, int mInvOffset,
                                    float[] m, int mOffset);

    public abstract void transposeM(float[] mTrans, int mTransOffset,
                                    float[] m, int mOffset);

    public abstract void multiplyMV(float[] resultVec, int resultVecOffset,
                                    float[] lhsMat, int lhsMatOffset,
                                    float[] rhsVec, int rhsVecOffset);

    public abstract void glClearColor(float r,float g,float b, float a);
}

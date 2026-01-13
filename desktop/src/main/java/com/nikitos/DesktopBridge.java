package com.nikitos;

import com.nikitos.platformBridge.PlatformBridge;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

public class DesktopBridge extends PlatformBridge {



    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {

    }

    @Override
    public void orthoM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far) {

    }

    @Override
    public void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far) {

    }

    @Override
    public void multiplyMM(float[] result, int resultOffset, float[] lhs, int lhsOffset, float[] rhs, int rhsOffset) {

    }

    @Override
    public void translateM(float[] m, int mOffset, float x, float y, float z) {

    }

    @Override
    public void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {

    }

    @Override
    public void scaleM(float[] m, int mOffset, float x, float y, float z) {

    }

    @Override
    public void setIdentityM(float[] sm, int smOffset) {

    }

    @Override
    public boolean invertM(float[] mInv, int mInvOffset, float[] m, int mOffset) {
        return false;
    }

    @Override
    public void transposeM(float[] mTrans, int mTransOffset, float[] m, int mOffset) {

    }

    @Override
    public void multiplyMV(float[] resultVec, int resultVecOffset, float[] lhsMat, int lhsMatOffset, float[] rhsVec, int rhsVecOffset) {

    }

    @Override
    public void glClearColor(float r, float g, float b, float a) {
        GL.createCapabilities();
        // Set the clear color
        GL32.glClearColor(r, g, b, a);
    }
}

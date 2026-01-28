package com.seal.gl_engine.platform;

import android.opengl.Matrix;
import com.nikitos.main.camera.CameraSettings;
import com.nikitos.main.camera.ProjectionMatrixSettings;
import com.nikitos.platformBridge.MatrixPlatformBridge;
import com.seal.gl_engine.engine.main.shaders.Shader;

import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class MatrixPlatformBridgeAndroid extends MatrixPlatformBridge {

    @Override
    public void setLookAtM(float[] rm, int rmOffset,
                           float eyeX, float eyeY, float eyeZ,
                           float centerX, float centerY, float centerZ,
                           float upX, float upY, float upZ) {
        android.opengl.Matrix.setLookAtM(rm, rmOffset, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ, upX, upY, upZ);
    }

    @Override
    public void orthoM(float[] m, int offset,
                       float left, float right, float bottom, float top,
                       float near, float far) {
        android.opengl.Matrix.orthoM(m, offset, left, right, bottom, top, near, far);
    }

    @Override
    public void frustumM(float[] m, int offset,
                         float left, float right, float bottom, float top,
                         float near, float far) {
        android.opengl.Matrix.frustumM(m, offset, left, right, bottom, top, near, far);
    }

    @Override
    public void multiplyMM(float[] result, int resultOffset,
                           float[] lhs, int lhsOffset,
                           float[] rhs, int rhsOffset) {
        android.opengl.Matrix.multiplyMM(result, resultOffset, lhs, lhsOffset, rhs, rhsOffset);
    }

    @Override
    public void multiplyMV(float[] resultVec, int resultVecOffset,
                           float[] lhsMat, int lhsMatOffset,
                           float[] rhsVec, int rhsVecOffset) {
        android.opengl.Matrix.multiplyMV(resultVec, resultVecOffset, lhsMat, lhsMatOffset, rhsVec, rhsVecOffset);
    }

    @Override
    public void translateM(float[] m, int mOffset,
                           float x, float y, float z) {
        android.opengl.Matrix.translateM(m, mOffset, x, y, z);
    }

    @Override
    public void rotateM(float[] m, int mOffset,
                        float a, float x, float y, float z) {
        android.opengl.Matrix.rotateM(m, mOffset, a, x, y, z);
    }

    @Override
    public void scaleM(float[] m, int mOffset,
                       float x, float y, float z) {
        android.opengl.Matrix.scaleM(m, mOffset, x, y, z);
    }

    @Override
    public void setIdentityM(float[] sm, int smOffset) {
        android.opengl.Matrix.setIdentityM(sm, smOffset);
    }

    @Override
    public boolean invertM(float[] mInv, int mInvOffset,
                           float[] m, int mOffset) {
        return android.opengl.Matrix.invertM(mInv, mInvOffset, m, mOffset);
    }

    @Override
    public void transposeM(float[] mTrans, int mTransOffset,
                           float[] m, int mOffset) {
        android.opengl.Matrix.transposeM(mTrans, mTransOffset, m, mOffset);
    }
    @Override
    public void bindAllMatrix(CameraSettings c, ProjectionMatrixSettings p, float[] mMatrix) {
        applyMatrix(mMatrix);
        applyProjectionMatrix(p);
        applyCameraSettings(c);
    }
    @Override
    public void applyProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled) {
        //choose wehther use perspective or not
        float[] mProjectionMatrix = new float[16];
        passProjectionMatrix(p, perspectiveEnabled, mProjectionMatrix);
    }

    public static void passProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled, float[] mProjectionMatrix) {
        if (perspectiveEnabled) {
            Matrix.frustumM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        } else {
            Matrix.orthoM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        }
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getProjectionLocation(), 1, false, mProjectionMatrix, 0);
    }
    @Override
    public void applyProjectionMatrix(ProjectionMatrixSettings p) {
        float[] mProjectionMatrix = new float[16];
        //perspective is always enabled here
        Matrix.frustumM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getProjectionLocation(), 1, false, mProjectionMatrix, 0);
    }
    @Override
    public void applyCameraSettings(CameraSettings cam) {
        float[] mViewMatrix = new float[16];
        Matrix.setLookAtM(mViewMatrix, 0, cam.eyeX, cam.eyeY, cam.eyeZ, cam.centerX, cam.centerY, cam.centerZ, cam.upX, cam.upY, cam.upZ);
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getCameraLocation(), 1, false, mViewMatrix, 0);
        glUniform3f(Shader.getActiveShader().getAdaptor().getCameraPosLlocation(), cam.eyeX, cam.eyeY, cam.eyeZ);
    }
    @Override
    public void applyMatrix(float[] mMatrix) {
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getTransformMatrixLocation(), 1, false, mMatrix, 0);
    }

}

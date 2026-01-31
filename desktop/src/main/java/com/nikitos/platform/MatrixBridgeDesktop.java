package com.nikitos.platform;

import com.nikitos.main.camera.CameraSettings;
import com.nikitos.main.camera.ProjectionMatrixSettings;
import com.nikitos.main.shaders.Shader;
import com.nikitos.platformBridge.MatrixPlatformBridge;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class MatrixBridgeDesktop extends MatrixPlatformBridge {
    @Override
    public void bindAllMatrix(CameraSettings c, ProjectionMatrixSettings p, float[] mMatrix) {
        applyMatrix(mMatrix);
        applyProjectionMatrix(p);
        applyCameraSettings(c);
    }

    @Override
    public void applyProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled) {
        float[] matrixArray = new float[16];
        if (perspectiveEnabled) {
            frustumM(matrixArray, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        } else {
            orthoM(matrixArray, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        }
         glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getProjectionLocation(),false, matrixArray);
    }

    @Override
    public void applyProjectionMatrix(ProjectionMatrixSettings p) {
        float[] matrixArray = new float[16];
        frustumM(matrixArray, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
         glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getProjectionLocation(),false, matrixArray);
    }

    @Override
    public void applyCameraSettings(CameraSettings cam) {
        float[] matrixArray = new float[16];
        setLookAtM(matrixArray, 0,
                cam.eyeX, cam.eyeY, cam.eyeZ,
                cam.centerX, cam.centerY, cam.centerZ,
                cam.upX, cam.upY, cam.upZ);

        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getCameraLocation(),false, matrixArray);
        glUniform3f(Shader.getActiveShader().getAdaptor().getCameraPosLlocation(), cam.eyeX, cam.eyeY, cam.eyeZ);
    }

    @Override
    public void applyMatrix(float[] mMatrix) {
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getTransformMatrixLocation(), false, mMatrix);
    }


    @Override
    public void setLookAtM(float[] rm, int rmOffset,
                           float eyeX, float eyeY, float eyeZ,
                           float centerX, float centerY, float centerZ,
                           float upX, float upY, float upZ) {
        Matrix4f tempMatrix = new Matrix4f();
        // Создаем матрицу вида (look-at) аналогично gluLookAt
        tempMatrix.identity()
                .lookAt(eyeX, eyeY, eyeZ,
                        centerX, centerY, centerZ,
                        upX, upY, upZ)
                .get(rm, rmOffset);
    }

    @Override
    public void orthoM(float[] m, int offset,
                       float left, float right, float bottom, float top,
                       float near, float far) {
        Matrix4f tempMatrix = new Matrix4f();
        // Ортографическая проекция
        tempMatrix.identity()
                .ortho(left, right, bottom, top, near, far)
                .get(m, offset);
    }

    @Override
    public void frustumM(float[] m, int offset,
                         float left, float right, float bottom, float top,
                         float near, float far) {
        Matrix4f tempMatrix = new Matrix4f();
        // Перспективная проекция через усеченную пирамиду
        tempMatrix.identity()
                .frustum(left, right, bottom, top, near, far)
                .get(m, offset);
    }

    @Override
    public void multiplyMM(float[] result, int resultOffset,
                           float[] lhs, int lhsOffset,
                           float[] rhs, int rhsOffset) {
        Matrix4f leftMatrix = new Matrix4f();
        Matrix4f tempMatrix = new Matrix4f();
        Matrix4f rightMatrix = new Matrix4f();
        // Умножение матриц: result = lhs × rhs
        leftMatrix.set(lhs, lhsOffset);
        rightMatrix.set(rhs, rhsOffset);
        leftMatrix.mul(rightMatrix, tempMatrix);
        tempMatrix.get(result, resultOffset);
    }

    @Override
    public void multiplyMV(float[] resultVec, int resultVecOffset,
                           float[] lhsMat, int lhsMatOffset,
                           float[] rhsVec, int rhsVecOffset) {
        Matrix4f tempMatrix = new Matrix4f();
        // Умножение матрицы на вектор: resultVec = lhsMat × rhsVec
        // Вектор предполагается однородным (4 компонента)
        tempMatrix.set(lhsMat, lhsMatOffset);

        Vector4f tempVector = new Vector4f();
        // Создаем вектор из массива
        tempVector.set(rhsVec[rhsVecOffset],
                rhsVec[rhsVecOffset + 1],
                rhsVec[rhsVecOffset + 2],
                rhsVec[rhsVecOffset + 3]);

        // Умножаем матрицу на вектор
        tempMatrix.transform(tempVector);

        // Записываем результат обратно в массив
        resultVec[resultVecOffset] = tempVector.x;
        resultVec[resultVecOffset + 1] = tempVector.y;
        resultVec[resultVecOffset + 2] = tempVector.z;
        resultVec[resultVecOffset + 3] = tempVector.w;
    }

    @Override
    public void translateM(float[] m, int mOffset,
                           float x, float y, float z) {
        Matrix4f tempMatrix = new Matrix4f();
        // Трансляция (смещение) матрицы
        tempMatrix.set(m, mOffset)
                .translate(x, y, z)
                .get(m, mOffset);
    }

    @Override
    public void rotateM(float[] m, int mOffset,
                        float a, float x, float y, float z) {
        Matrix4f tempMatrix = new Matrix4f();
        float length = (float) Math.pow(x*x+y*y+z*z,0.5);
        // Вращение матрицы на угол 'a' градусов вокруг оси (x, y, z)
        // JOML работает с радианами, поэтому конвертируем
        float angleRad = (float) Math.toRadians(a);
        tempMatrix.set(m, mOffset)
                .rotate(angleRad, x/length, y/length, z/length)
                .get(m, mOffset);
    }

    @Override
    public void scaleM(float[] m, int mOffset,
                       float x, float y, float z) {
        Matrix4f tempMatrix = new Matrix4f();
        // Масштабирование матрицы
        tempMatrix.set(m, mOffset)
                .scale(x, y, z)
                .get(m, mOffset);
    }

    @Override
    public void setIdentityM(float[] sm, int smOffset) {
        Matrix4f tempMatrix = new Matrix4f();
        // Установка единичной матрицы
        tempMatrix.identity().get(sm, smOffset);
    }

    @Override
    public boolean invertM(float[] mInv, int mInvOffset,
                           float[] m, int mOffset) {
        Matrix4f sourceMatrix = new Matrix4f();
        // Инверсия матрицы, возвращает false если матрица вырожденная
        sourceMatrix.set(m, mOffset);
        if (sourceMatrix.determinant() < Float.MIN_VALUE) {
            return false;
        }
        sourceMatrix.invert().get(mInv, mInvOffset);
        return true;
    }

    @Override
    public void transposeM(float[] mTrans, int mTransOffset,
                           float[] m, int mOffset) {
        Matrix4f sourceMatrix = new Matrix4f();
        // Транспонирование матрицы
        sourceMatrix.set(m, mOffset)
                .transpose()
                .get(mTrans, mTransOffset);
    }

}

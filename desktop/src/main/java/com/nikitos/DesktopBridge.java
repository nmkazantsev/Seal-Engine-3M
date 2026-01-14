package com.nikitos;

import com.nikitos.platformBridge.PlatformBridge;
import org.joml.Matrix4f;
import org.joml.Vector4f;


public class DesktopBridge extends PlatformBridge {


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    // Временные объекты для минимизации аллокаций в циклах рендеринга
    private final Matrix4f tempMatrix = new Matrix4f();
    private final Vector4f tempVector = new Vector4f();
    private final Matrix4f leftMatrix = new Matrix4f();
    private final Matrix4f rightMatrix = new Matrix4f();
    private final Matrix4f sourceMatrix = new Matrix4f();

    @Override
    public void setLookAtM(float[] rm, int rmOffset,
                           float eyeX, float eyeY, float eyeZ,
                           float centerX, float centerY, float centerZ,
                           float upX, float upY, float upZ) {
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
        // Ортографическая проекция
        tempMatrix.identity()
                .ortho(left, right, bottom, top, near, far)
                .get(m, offset);
    }

    @Override
    public void frustumM(float[] m, int offset,
                         float left, float right, float bottom, float top,
                         float near, float far) {
        // Перспективная проекция через усеченную пирамиду
        tempMatrix.identity()
                .frustum(left, right, bottom, top, near, far)
                .get(m, offset);
    }

    @Override
    public void multiplyMM(float[] result, int resultOffset,
                           float[] lhs, int lhsOffset,
                           float[] rhs, int rhsOffset) {
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
        // Умножение матрицы на вектор: resultVec = lhsMat × rhsVec
        // Вектор предполагается однородным (4 компонента)
        tempMatrix.set(lhsMat, lhsMatOffset);

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
        // Трансляция (смещение) матрицы
        tempMatrix.set(m, mOffset)
                .translate(x, y, z)
                .get(m, mOffset);
    }

    @Override
    public void rotateM(float[] m, int mOffset,
                        float a, float x, float y, float z) {
        // Вращение матрицы на угол 'a' градусов вокруг оси (x, y, z)
        // JOML работает с радианами, поэтому конвертируем
        float angleRad = (float) Math.toRadians(a);
        tempMatrix.set(m, mOffset)
                .rotate(angleRad, x, y, z)
                .get(m, mOffset);
    }

    @Override
    public void scaleM(float[] m, int mOffset,
                       float x, float y, float z) {
        // Масштабирование матрицы
        tempMatrix.set(m, mOffset)
                .scale(x, y, z)
                .get(m, mOffset);
    }

    @Override
    public void setIdentityM(float[] sm, int smOffset) {
        // Установка единичной матрицы
        tempMatrix.identity().get(sm, smOffset);
    }

    @Override
    public boolean invertM(float[] mInv, int mInvOffset,
                           float[] m, int mOffset) {
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
        // Транспонирование матрицы
        sourceMatrix.set(m, mOffset)
                .transpose()
                .get(mTrans, mTransOffset);
    }

    @Override
    public void glClearColor(float r, float g, float b, float a) {
        // Установка цвета очистки экрана
        glClearColor(r, g, b, a);
    }

    /**
     * Дополнительный метод: создание перспективной проекции (аналог gluPerspective)
     * Не входит в абстрактный класс, но полезен для 3D-графики.
     */
    public void perspectiveM(float[] m, int offset,
                             float fovy, float aspect, float near, float far) {
        tempMatrix.identity()
                .perspective((float) Math.toRadians(fovy), aspect, near, far)
                .get(m, offset);
    }

    /**
     * Дополнительный метод: копирование матрицы
     */
    public void copyMatrix(float[] dest, int destOffset,
                           float[] src, int srcOffset) {
        System.arraycopy(src, srcOffset, dest, destOffset, 16);
    }


}

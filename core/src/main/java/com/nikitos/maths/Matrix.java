package com.nikitos.maths;

import com.nikitos.platformBridge.MatrixPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;

public class Matrix {

    private static MatrixPlatformBridge matrixPlatformBridge;

    public static void init(PlatformBridge platformBridge) {
        Matrix.matrixPlatformBridge = platformBridge.getMatrixPlatformBridge();
    }

    /**
     * create new I matrix
     *
     * @return new float [16]  - I matrix
     */
    public static float[] I() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    public static float[] resetTranslateMatrix(float[] arr) {
        arr = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
        return arr;
    }

    /**
     * Устанавливает матрицу вида (look-at matrix) на основе параметров камеры.
     *
     * @param rm       массив результата длиной 16
     * @param rmOffset смещение в массиве результата
     * @param eyeX     координата X положения камеры
     * @param eyeY     координата Y положения камеры
     * @param eyeZ     координата Z положения камеры
     * @param centerX  координата X цели камеры
     * @param centerY  координата Y цели камеры
     * @param centerZ  координата Z цели камеры
     * @param upX      координата X вектора "вверх"
     * @param upY      координата Y вектора "вверх"
     * @param upZ      координата Z вектора "вверх"
     */
    public static void setLookAtM(float[] rm, int rmOffset,
                                  float eyeX, float eyeY, float eyeZ,
                                  float centerX, float centerY, float centerZ,
                                  float upX, float upY, float upZ) {
        matrixPlatformBridge.setLookAtM(rm, rmOffset, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ, upX, upY, upZ);
    }

    /**
     * Создает ортографическую матрицу проекции.
     *
     * @param m      массив результата длиной 16
     * @param offset смещение в массиве результата
     * @param left   координата левой плоскости отсечения
     * @param right  координата правой плоскости отсечения
     * @param bottom координата нижней плоскости отсечения
     * @param top    координата верхней плоскости отсечения
     * @param near   расстояние до ближней плоскости отсечения (положительное)
     * @param far    расстояние до дальней плоскости отсечения (положительное)
     */
    public static void orthoM(float[] m, int offset,
                              float left, float right, float bottom, float top,
                              float near, float far) {
        matrixPlatformBridge.orthoM(m, offset, left, right, bottom, top, near, far);
    }

    /**
     * Создает матрицу перспективной проекции (усеченная пирамида).
     *
     * @param m      массив результата длиной 16
     * @param offset смещение в массиве результата
     * @param left   координата левой плоскости отсечения на ближнем расстоянии
     * @param right  координата правой плоскости отсечения на ближнем расстоянии
     * @param bottom координата нижней плоскости отсечения на ближнем расстоянии
     * @param top    координата верхней плоскости отсечения на ближнем расстоянии
     * @param near   расстояние до ближней плоскости отсечения (положительное)
     * @param far    расстояние до дальней плоскости отсечения (положительное)
     */

    public static void frustumM(float[] m, int offset,
                                float left, float right, float bottom, float top,
                                float near, float far) {
        matrixPlatformBridge.frustumM(m, offset, left, right, bottom, top, near, far);
    }

    /**
     * Умножает две матрицы 4x4.
     *
     * @param result       массив результата длиной 16
     * @param resultOffset смещение в массиве результата
     * @param lhs          массив левой матрицы длиной 16
     * @param lhsOffset    смещение в массиве левой матрицы
     * @param rhs          массив правой матрицы длиной 16
     * @param rhsOffset    смещение в массиве правой матрицы
     */

    public static void multiplyMM(float[] result, int resultOffset,
                                  float[] lhs, int lhsOffset,
                                  float[] rhs, int rhsOffset) {
        matrixPlatformBridge.multiplyMM(result, resultOffset, lhs, lhsOffset, rhs, rhsOffset);
    }

    /**
     * Применяет трансляцию (сдвиг) к матрице 4x4.
     * Эквивалентно умножению матрицы на матрицу трансляции.
     *
     * @param m       массив матрицы для преобразования и результата длиной 16
     * @param mOffset смещение в массиве матрицы
     * @param x       смещение по оси X
     * @param y       смещение по оси Y
     * @param z       смещение по оси Z
     */

    public static void translateM(float[] m, int mOffset,
                                  float x, float y, float z) {
        matrixPlatformBridge.translateM(m, mOffset, x, y, z);
    }

    /**
     * Применяет поворот к матрице 4x4.
     * Эквивалентно умножению матрицы на матрицу поворота.
     *
     * @param m       массив матрицы для преобразования и результата длиной 16
     * @param mOffset смещение в массиве матрицы
     * @param a       угол поворота в градусах
     * @param x       компонента X оси вращения
     * @param y       компонента Y оси вращения
     * @param z       компонента Z оси вращения
     */

    public static void rotateM(float[] m, int mOffset,
                               float a, float x, float y, float z) {
        matrixPlatformBridge.rotateM(m, mOffset, a, x, y, z);
    }

    /**
     * Применяет масштабирование к матрице 4x4.
     * Эквивалентно умножению матрицы на матрицу масштабирования.
     *
     * @param m       массив матрицы для преобразования и результата длиной 16
     * @param mOffset смещение в массиве матрицы
     * @param x       масштаб по оси X
     * @param y       масштаб по оси Y
     * @param z       масштаб по оси Z
     */

    public static void scaleM(float[] m, int mOffset,
                              float x, float y, float z) {
        matrixPlatformBridge.scaleM(m, mOffset, x, y, z);
    }

    /**
     * Инициализирует матрицу как единичную (матрица без преобразований).
     *
     * @param sm       массив результата длиной 16
     * @param smOffset смещение в массиве результата
     */
    public static void setIdentityM(float[] sm, int smOffset) {
        matrixPlatformBridge.setIdentityM(sm, smOffset);
    }

    /**
     * Вычисляет обратную матрицу.
     *
     * @param mInv       массив для обратной матрицы длиной 16
     * @param mInvOffset смещение в массиве обратной матрицы
     * @param m          массив исходной матрицы длиной 16
     * @param mOffset    смещение в массиве исходной матрицы
     * @return true, если обратная матрица существует и успешно вычислена,
     * false, если матрица вырождена (определитель равен 0)
     */

    public static boolean invertM(float[] mInv, int mInvOffset,
                                  float[] m, int mOffset) {
        return matrixPlatformBridge.invertM(mInv, mInvOffset, m, mOffset);
    }

    /**
     * Вычисляет транспонированную матрицу.
     *
     * @param mTrans       массив для транспонированной матрицы длиной 16
     * @param mTransOffset смещение в массиве транспонированной матрицы
     * @param m            массив исходной матрицы длиной 16
     * @param mOffset      смещение в массиве исходной матрицы
     */

    public static void transposeM(float[] mTrans, int mTransOffset,
                                  float[] m, int mOffset) {
        matrixPlatformBridge.transposeM(mTrans, mTransOffset, m, mOffset);
    }

    /**
     * Умножает матрицу 4x4 на 4-мерный вектор.
     * Выполняет операцию resultVec = lhsMat × rhsVec, где lhsMat - матрица 4x4,
     * а rhsVec - вектор-столбец [x, y, z, w].
     *
     * @param resultVec       массив для результата длиной не менее 4
     * @param resultVecOffset смещение в массиве результата
     * @param lhsMat          массив матрицы 4x4 длиной 16
     * @param lhsMatOffset    смещение в массиве матрицы
     * @param rhsVec          массив исходного вектора длиной не менее 4
     * @param rhsVecOffset    смещение в массиве исходного вектора
     */
    public static void multiplyMV(float[] resultVec, int resultVecOffset,
                                  float[] lhsMat, int lhsMatOffset,
                                  float[] rhsVec, int rhsVecOffset) {
        matrixPlatformBridge.multiplyMV(resultVec, resultVecOffset, lhsMat, lhsMatOffset, rhsVec, rhsVecOffset);
    }
}


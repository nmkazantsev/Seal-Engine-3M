package com.nikitos.platformBridge;

import com.nikitos.main.camera.CameraSettings;
import com.nikitos.main.camera.ProjectionMatrixSettings;

/**
 * an abstract class to be implemented by platform - dependent implementations
 */
public abstract class PlatformBridge {
    public abstract void onPause();

    public abstract void onResume();


    /**
     * Привязывает все матрицы (модели, проекции и вида) к шейдеру.
     *
     * @param c       настройки камеры (матрица вида)
     * @param p       настройки проекции (матрица проекции)
     * @param mMatrix матрица модели
     */
    public abstract void bindAllMatrix(CameraSettings c, ProjectionMatrixSettings p, float[] mMatrix);

    /**
     * Применяет матрицу проекции с возможностью выбора между перспективной и ортографической проекцией.
     *
     * @param p                  настройки проекции
     * @param perspectiveEnabled true - использовать перспективную проекцию, false - ортографическую
     */
    public abstract void applyProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled);

    /**
     * Применяет матрицу проекции (всегда перспективная проекция).
     *
     * @param p настройки проекции
     */
    public abstract void applyProjectionMatrix(ProjectionMatrixSettings p);

    /**
     * Применяет настройки камеры (матрицу вида и позицию камеры).
     *
     * @param cam настройки камеры
     */
    public abstract void applyCameraSettings(CameraSettings cam);

    /**
     * Применяет матрицу модели/преобразования.
     *
     * @param mMatrix матрица модели (4x4 в виде float[16])
     */
    public abstract void applyMatrix(float[] mMatrix);

    /**
     * Создает перспективную матрицу проекции.
     *
     * @param result массив для результата (должен быть float[16])
     * @param offset смещение в массиве
     * @param left   левая плоскость отсечения
     * @param right  правая плоскость отсечения
     * @param bottom нижняя плоскость отсечения
     * @param top    верхняя плоскость отсечения
     * @param near   ближняя плоскость отсечения
     * @param far    дальняя плоскость отсечения
     */
    public abstract void frustumM(float[] result, int offset,
                                  float left, float right,
                                  float bottom, float top,
                                  float near, float far);

    /**
     * Создает ортографическую матрицу проекции.
     *
     * @param result массив для результата (должен быть float[16])
     * @param offset смещение в массиве
     * @param left   левая плоскость отсечения
     * @param right  правая плоскость отсечения
     * @param bottom нижняя плоскость отсечения
     * @param top    верхняя плоскость отсечения
     * @param near   ближняя плоскость отсечения
     * @param far    дальняя плоскость отсечения
     */
    public abstract void orthoM(float[] result, int offset,
                                float left, float right,
                                float bottom, float top,
                                float near, float far);

    /**
     * Создает матрицу вида (look-at матрицу).
     *
     * @param result  массив для результата (должен быть float[16])
     * @param offset  смещение в массиве
     * @param eyeX    координата X позиции камеры
     * @param eyeY    координата Y позиции камеры
     * @param eyeZ    координата Z позиции камеры
     * @param centerX координата X точки, на которую направлена камера
     * @param centerY координата Y точки, на которую направлена камера
     * @param centerZ координата Z точки, на которую направлена камера
     * @param upX     координата X вектора "вверх"
     * @param upY     координата Y вектора "вверх"
     * @param upZ     координата Z вектора "вверх"
     */
    public abstract void setLookAtM(float[] result, int offset,
                                    float eyeX, float eyeY, float eyeZ,
                                    float centerX, float centerY, float centerZ,
                                    float upX, float upY, float upZ);


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

    public abstract void glClearColor(float r, float g, float b, float a);
}

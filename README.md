The first cross-platform version of https://github.com/nmkazantsev/seal_engine.

All features are suppoerted. Unified for all platforms (Android, Windows, Linux) api was not changed.

GLSL resources are checked out with LF line endings on every platform so the
reproducible Gradle JAR settings produce the same runtime artifacts on Linux,
macOS, and Windows.

See example apps:
Desktop: https://github.com/nmkazantsev/Demo-launcher

Android: https://github.com/nmkazantsev/Demo-app



# Документация пользователя ядра Seal Engine 3-M

## Введение

Данный документ описывает основные классы и методы игрового движка **Seal Engine 3-M** (версия 3.2.4). Движок предназначен для создания 2D и 3D игр с использованием OpenGL. Архитектура построена вокруг страниц (`GamePageClass`), камеры, шейдеров, вершинных объектов и системы анимации.

Документ сгруппирован по функциональным разделам. Для каждого класса приведено краткое описание и список публичных методов, которые могут быть полезны разработчику.

---

## создание приложений

для создания нового приложения используйте генератор, скачайте последнюю версию с gitHub https://github.com/nmkazantsev/seal-app-generator .

---

## руководство по созданию страницы движка
1. создать имплементацию ``GamePageClass``.
2. В конструкторе (или заранее) загружать тяжелые объекты, такие, как меши и картинки, а также шейдеры и шрифты.
3. в onSurfaceChanged пересоздавать камеру, frame buffers и другие объекты, которые прямо или косвенно зависят от разрешения экрана. 
4. при отрисовке каждый кадр нужно: подключить шейдер, подключить матрицу проекции и камеру. Отрисовать сцену.
5. в целях адаптации, обработчик касаний не должен ни где кешировать границы, они должны вычисляться динамически, чтобы изменение размеров экрана не повлияло на его работу.
6. рекомендуется разделять контекст на страницы и избегать использования статических объектов, так как неаккуратное обращение с ними спровоцирует утечку видеопамяти. Для их удаления не забывать запускать метод очисти видеопамяти.

## 1. Ядро движка и управление страницами

### Engine
Главный класс движка. Управляет жизненным циклом, переключением страниц, FPS.

**Публичные методы:**
- `static String getVersion()` – возвращает версию движка (например, "v3.2.0").
- `void startNewPage(GamePageClass newPage)` – переключает текущую игровую страницу. Старая страница удаляется сборщиком мусора.
- `long pageMillis()` – возвращает время в миллисекундах с момента загрузки текущей страницы.
- `void glClear()` – очищает буфер цвета и глубины.
- `void disableBlend()` – отключает смешивание цветов.
- `void enableBlend()` – включает смешивание цветов.
- `Platform getPlatform()` – возвращает платформу (DESKTOP или MOBILE).
- `String loadTextFile(String path)` – читает UTF-8 текстовый файл из runtime filesystem.
- `void saveTextFile(String path, String text)` – сохраняет UTF-8 текстовый файл в runtime filesystem. Родительская папка должна уже существовать.
- `boolean fileExists(String path)` – возвращает true только если путь указывает на существующий обычный файл.
- `boolean folderExists(String path)` – возвращает true только если путь указывает на существующую папку.
- `boolean createFolder(String path)` – рекурсивно создаёт папку и недостающие родительские папки.
- `void disableMouseCursor()` – скрывает/захватывает курсор мыши на desktop; на Android безопасный no-op.
- `void enableMouseCursor()` – возвращает обычный курсор мыши на desktop; на Android безопасный no-op.
- `void setMousePosition(float x, float y)` – устанавливает позицию курсора в координатах окна на desktop; на Android безопасный no-op.
- `float fps` – публичное поле, содержащее текущий FPS.

**Правила путей для runtime filesystem (`Engine.*File*` / `createFolder`)**
- Эти методы работают только с runtime filesystem и не используют assets / classpath ресурсы.
- Все текстовые операции используют UTF-8.
- Абсолютный путь определяется платформенным `File.isAbsolute()` и используется как есть.
- Относительный путь всегда сначала привязывается к platform-specific runtime root, затем нормализуется (`.` / `..`), и не может выйти за пределы этого root.
- Пустой или пробельный путь считается невалидным.
- Если путь существует, но имеет неверный тип, поведение строгое:
  - `fileExists(...)` вернёт `false` для папки
  - `folderExists(...)` вернёт `false` для обычного файла
  - `loadTextFile(...)` и `saveTextFile(...)` выбросят `RuntimeException`
  - `createFolder(...)` вернёт `false`, если по этому пути уже существует обычный файл
- `saveTextFile(...)` не создаёт родительские папки автоматически; для этого сначала вызовите `createFolder(...)`.

**Platform-specific runtime root для относительных путей**
- Desktop: текущая рабочая директория приложения (`System.getProperty("user.dir")`).
- Android: internal app files directory, возвращаемая `Context.getFilesDir()`, обычно путь вида `/data/user/0/<package>/files` или `/data/data/<package>/files`.
- Android note: relative runtime files теперь сознательно мапятся в реальную writable app-internal directory. Это не `assets`, не classpath resources и не external/shared storage. Все операции `save/load/fileExists/folderExists/createFolder` используют этот же root и те же path rules.

**BSOD / Crash Screen**
- Если BSOD support включён через `LauncherParams.setUseBSOD(true)` во время запуска, движок инициализирует automatic crash-screen handling при старте.
- При исключении внутри пользовательского приложения движок автоматически показывает `BSODScreen`.
- Экран отображает информацию об ошибке на экране и параллельно сохраняет её в текстовый файл.
- Crash log locations:
  - Desktop: `crashes` folder inside the application folder
  - Android: `Android/data/<app>/files/crashes`
- Crash log сохраняется как `.txt` файл; имя файла содержит дату и время ошибки.

### GamePageClass
Абстрактный класс, от которого должны наследоваться все игровые страницы.

**Публичные методы:**
- `void onInstalled()` – lifecycle hook, вызываемый `Engine.startNewPage(...)` после установки точного экземпляра страницы, до первого `onSurfaceChanged(...)` и до очистки реестров исходящей страницы. Реализация по умолчанию ничего не делает.
- `abstract void onSurfaceChanged(int x, int y)` – вызывается при изменении размеров экрана.
- `abstract void draw()` – основной метод отрисовки, вызывается каждый кадр.
- `abstract void onResume()` – вызывается при возврате приложения на передний план.
- `abstract void onPause()` – вызывается при уходе приложения в фон.

Неположительный размер поверхности (например, промежуточный `0x0` при
минимизации окна) не передаётся странице. Движок сохраняет последние
положительные размеры и коэффициенты до следующего пригодного callback; поэтому
переход страницы во время минимизации получает последний usable viewport.

### LauncherParams
Класс для настройки параметров запуска движка. Используется при создании экземпляра `Engine`.

**Публичные методы (цепочка вызовов):**
- `LauncherParams setDebug(boolean debug)` – включает/выключает режим отладки.
- `LauncherParams setWindowTitle(String windowTitle)` – устанавливает заголовок окна (для десктопа).
- `LauncherParams setFullScreen(boolean fullScreen)` – включает/выключает полноэкранный режим.
- `LauncherParams setMSAA(boolean MSAA)` – включает/выключает мультисэмплинг.
- `LauncherParams setStartPage(Function<Void, GamePageClass> startPage)` – задаёт поставщик стартовой страницы.
- `LauncherParams setRuntimeObserver(RuntimeObserver runtimeObserver)` – задаёт необязательный runtime observer. По умолчанию значение равно `null`.
- `LauncherParams setWindowSize(int width, int height)` – задаёт положительные ширину и высоту desktop-окна как одну неделимую настройку.
- `LauncherParams setMaximized(boolean maximized)` – управляет стартовой максимизацией оконного desktop-режима.
- `LauncherParams setVSync(boolean vSync)` – задаёт desktop swap interval: `1` при `true` и `0` при `false`.
- `LauncherParams setDesktopOpenGl33CoreContext(boolean enabled)` – явно запрашивает desktop-контекст OpenGL 3.3 core profile для диагностических инструментов, которым несовместим legacy-контекст.
- `boolean hasWindowSize()`, `Integer getWindowWidth()`, `Integer getWindowHeight()`, `boolean getMaximized()`, `boolean getVSync()`, `boolean isDesktopOpenGl33CoreContext()` – геттеры desktop-настроек окна и контекста.
- `boolean isDebug()`, `boolean getMSAA()`, `boolean isDesktop()`, `String getWindowTitle()`, `boolean getFullScreen()`, `RuntimeObserver getRuntimeObserver()` – остальные геттеры.

Legacy defaults desktop-окна и OpenGL-контекста сохранены: явный размер отсутствует, окно максимизируется, VSync включён, а запрос OpenGL 3.3 core profile выключен. При значении `false` новый параметр не добавляет GLFW hints и не меняет Android. Для воспроизводимого оконного запуска 1280×720 без VSync используйте:

```java
new LauncherParams()
        .setFullScreen(false)
        .setWindowSize(1280, 720)
        .setMaximized(false)
        .setVSync(false);
```

Размер и максимизация применяются только к оконному desktop-режиму. `CoreRenderer` получает фактический размер framebuffer, который может отличаться от размера окна на HiDPI-системах.

### DesktopLauncher window control

`DesktopLauncher` предоставляет узкий desktop-only API управления уже созданным GLFW-окном:

- `void requestStop()` устанавливает close flag окна. Существующий render loop завершает текущую итерацию, выходит по своему штатному условию и выполняет обычное освобождение callbacks, окна, GLFW и audio; `System.exit` не используется.
- `void requestWindowSize(int width, int height)` запрашивает положительный размер content area в screen coordinates. Это не размер framebuffer в pixels; фактический framebuffer и `onSurfaceChanged(...)` продолжают обновляться существующим GLFW callback.
- `void requestPage(GamePageClass page)` синхронно делегирует точный экземпляр страницы в `Engine.startNewPage(...)`. Метод предназначен для render-thread coordinator: он не раскрывает `Engine`, не создаёт очередь и сохраняет обычный `PageTransition`, включая переходы между разными экземплярами одного класса.

Все три метода должны вызываться из того же потока, который создал `DesktopLauncher` и выполняет его render loop. Окно активно после успешного завершения конструктора и до возврата `run()`. Вызов из другого потока или после teardown выбрасывает `IllegalStateException`; неположительный размер выбрасывает `IllegalArgumentException` без native-вызова. Для воспроизводимых resize-сценариев используйте оконный немаксимизированный режим: в full-screen GLFW трактует изменение размера как смену желаемого video mode.

API не меняет launcher defaults, порядок кадра, FPS, `Engine.pageMillis()` или `Utils.millis()` и не добавляет работу в render loop, если методы не вызываются.

### Runtime Observer API
`RuntimeObserver` задаёт необязательный API наблюдения за runtime. Его default-методы не выполняют действий: `beforeFrame(FrameContext)`, `afterFrame(FrameContext)`, `afterFrame(FrameContext, FrameCaptureSource)`, `onPageChanged(PageTransition)` и `onFailure(RuntimeFailure)`.

- `FrameContext` содержит идентификатор кадра, текущую `GamePageClass`, ширину, высоту, `Platform` и допускающий `null` `RuntimeResourceSnapshot`.
- `RuntimeResourceSnapshot` неизменно хранит числа отслеживаемых VRAM-объектов, shader-программ, `ShaderData`, touch processors, keyboard press/release/combo listeners и desktop mouse callback registrations. Прежний семиаргументный конструктор сохранён; в созданном им snapshot счётчик `ShaderData` равен `0`.
- Snapshot снимается только на observed-пути непосредственно перед `beforeFrame` и согласован с pre-frame значением `FrameContext.getPage()`. Поэтому на первом кадре до создания default page страница равна `null`, а counters описывают состояние до её конструктора.
- `PageTransition` содержит предыдущую и новую `GamePageClass`.
- `RuntimeFailure` содержит `Stage`, исходное `Throwable`, а также допускающие `null` `FrameContext` и `GamePageClass`. Возможные стадии: `FRAME_SETUP`, `PAGE_DRAW`, `DEBUGGER_DRAW`, `VERTICES_REDRAW`, `TOUCH_PROCESS`, `KEYBOARD_PROCESS`, `PAGE_TRANSITION`.
- `Engine.getRuntimeObserver()` возвращает observer, зафиксированный при создании `Engine`; отдельного runtime setter нет.
- При установленном observer идентификаторы кадров начинаются с `1`. Порядок вызовов: `beforeFrame` → расчёт FPS / стартовая страница / начало кадра → `GamePageClass.draw()` → `Debugger.draw()` → перерисовка вершин → touch → keyboard → `afterFrame`.
- После успешного перехода страницы вызывается `onPageChanged`; первый переход передаётся как `null → startPage`. Ошибка перехода передаётся в `onFailure` со стадией `PAGE_TRANSITION`, без `FrameContext`.
- Переход, вызванный приложением из `draw()` или другой стадии наблюдаемого кадра, сохраняет стадию `PAGE_TRANSITION`: его ошибка не отправляется повторно как ошибка окружающей стадии кадра.
- Ошибка стадии кадра передаётся в `onFailure` с исходной причиной, контекстом кадра и страницей, активной в момент ошибки. Существующее BSOD-поведение ошибки страницы сохраняется, а ошибки после страницы продолжают распространяться вызывающему коду.
- `Error` на наблюдаемом пути также передаётся с точной стадией и затем распространяется напрямую; BSOD создаётся только для прежних `Exception`-сбоев страницы/перехода.
- Если `onFailure` кадра сам выбрасывает исключение, исходная ошибка остаётся основной, а ошибка observer добавляется как suppressed. Ошибки `beforeFrame`, `afterFrame` и `onPageChanged` распространяются напрямую и повторно через `onFailure` не отправляются.

**On-demand frame capture**

- Движок вызывает двухаргументный `afterFrame(FrameContext, FrameCaptureSource)` в прежней точке окончания кадра. Его default-реализация делегирует старому `afterFrame(FrameContext)`, поэтому существующие observer остаются source-compatible.
- `FrameCaptureSource.isAvailable()` сообщает о доступности, а `capture()` синхронно захватывает текущий default framebuffer. Desktop-источник доступен после привязки GLFW-окна. Android-источник доступен только в callback потока `GLSurfaceView.Renderer`, когда EGL context current и поверхность имеет положительный размер.
- Desktop-захват разрешён только синхронно из callback render-потока, после полной отрисовки кадра и до `glfwSwapBuffers`. Размер запрашивается через framebuffer pixels, а не через screen coordinates окна.
- Android-захват выполняет `GLES30.glReadPixels(...)` в том же месте `afterFrame`, до возврата из `onDrawFrame(...)` и автоматического swap. Для плотного чтения из default framebuffer источник временно выбирает `GL_BACK`, отключает pixel-pack buffer и устанавливает `GL_PACK_ALIGNMENT = 1`, `GL_PACK_ROW_LENGTH = 0`, `GL_PACK_SKIP_ROWS = 0`, `GL_PACK_SKIP_PIXELS = 0`. Прежние read framebuffer/buffer и все перечисленные pack-состояния восстанавливаются отдельными best-effort операциями даже при ошибке; исходная ошибка чтения остаётся основной, а ошибки восстановления добавляются как suppressed.
- После Android surface/context recreation источник остаётся тем же объектом, но временно недоступен до следующего положительного `onSurfaceChanged(...)`. `capture()` отклоняет вызов вне зарегистрированного GL thread, без current EGL context, с неположительными или переполняющими Java-массив размерами.
- `CapturedFrame` хранит положительные `width`/`height` и ровно `width * height * 4` байта в порядке R, G, B, A. Нулевая строка — верхняя; входной массив и результат `getRgba()` копируются.
- Источник передаётся observer, но не выполняет readback сам. Буферы, массивы и `glReadPixels` появляются только при явном вызове `capture()`. Без observer нулевой путь кадра не запрашивает даже `FrameCaptureSource`.
- При `null` observer выполняется прежний lifecycle без чтения resource counters, создания runtime DTO, счётчика наблюдаемых кадров и observer callbacks.

**Android Activity recreation**

- Все `AndroidLauncher` внутри одного процесса используют один и тот же `Engine`, `AndroidBridge` и источник кадров. Новая Activity создаёт новый `GLSurfaceView`, который атомарно становится текущим; предыдущий view сначала останавливается. Настройки Engine и bridge хранят только application context и не удерживают первую Activity. Публичный `AndroidBridge()` сохраняет совместимость: application context привязывается при первом создании view.
- Передавайте точный view, возвращённый `launch()`, в `AndroidLauncher.onPause(view)`, `onResume(view)` и `detach(view)`. Текущий `GLSurfaceView` останавливается до `GamePageClass.onPause()` и `Utils.onPause()`, а повторный вызов bridge из `Engine.onPause()` безопасно игнорируется. Поздний callback старой Activity и повторный callback не меняют lifecycle страницы или учёт игрового времени.
- `OpenGLRenderer` не создаёт `CoreRenderer` в конструкторе на UI thread. Локальный renderer создаётся или заменяется только в положительном `onSurfaceChanged(...)` на GL thread; `onDrawFrame(...)` до этого безопасно ничего не делает.
- Существующие `launch()`, `getEngine()` и прямые `Engine.onPause()` / `Engine.onResume()` остаются доступными. Identity-aware методы launcher следует использовать для Activity lifecycle:

```java
GLSurfaceView gameView = launcher.launch();

// Activity callbacks:
launcher.onPause(gameView);
launcher.onResume(gameView);
launcher.detach(gameView);
```

---

## 2. Математические классы

### PVector
Представляет трёхмерный вектор с методами арифметики. Используется во многих частях движка.

**Конструкторы:**
- `PVector()` – нулевой вектор.
- `PVector(float x, float y, float z)`
- `PVector(float x, float y)` – z=0.
- `PVector(float v)` – все компоненты равны v.
- `PVector(Vec3 v)`, `PVector(PVector v)`
- `PVector(float[] arr, int i)` – читает три компоненты из массива, начиная с индекса i.

**Публичные методы:**
- `float[] getArray()` – возвращает массив {x, y, z}.
- `void normalize()` – нормализует вектор (side effect).
- `static PVector normalize(PVector v)` – возвращает нормализованную копию.
- `float length()` – длина вектора.
- `PVector add(PVector v)` – прибавляет вектор (изменяет текущий), возвращает this.
- `static PVector add(PVector v, PVector u)` – возвращает сумму.
- `PVector sub(PVector v)` – вычитает вектор (изменяет текущий).
- `static PVector sub(PVector v, PVector u)` – возвращает разность.
- `PVector mul(float i)` – умножает на скаляр (изменяет текущий).
- `static PVector mul(PVector v, float a)` – возвращает произведение.
- `PVector div(float i)` – делит на скаляр (изменяет текущий).
- `static PVector cross(PVector v, PVector u)` – векторное произведение.
- `void cross(PVector u)` – заменяет текущий вектор на векторное произведение.
- `static float getAngle(PVector v, PVector u)` – угол между векторами в радианах.
- `static PVector rotateToVec(PVector v, PVector u, float alpha)` – поворачивает v в направлении u на угол alpha.
- `static PVector rotateVec3(PVector vec, PVector axis, float a)` – поворачивает vec вокруг оси axis на угол a (градусы).
- `void rotateVec3(PVector axis, float a)` – поворачивает текущий вектор (side effect).
- `static float dot(PVector a, PVector b)` – скалярное произведение.

### Vec3
Аналогичен PVector, но неизменяемый (методы возвращают новые объекты). Используется в некоторых частях.

**Конструкторы:**
- `Vec3()`, `Vec3(float x, float y, float z)`, `Vec3(float v)`, `Vec3(Vec3 v)`, `Vec3(PVector v)`, `Vec3(float[] arr, int i)`.

**Публичные методы:**
- `float[] getArray()`
- `Vec3 normalize()`
- `float length()`
- `Vec3 add(Vec3 v)`, `Vec3 sub(Vec3 v)`, `Vec3 mul(float i)`, `Vec3 div(float i)` – возвращают новый вектор.
- `Vec3 cross(Vec3 a)`
- `float dot(Vec3 b)`
- `static float getAngle(Vec3 v, Vec3 u)`
- `Vec3 rotateVec3(Vec3 axis, float a)`
- `float getDirection(Vec3 b)` – угол от текущего вектора до b в градусах (против часовой стрелки, 2D).

### Matrix
Статический класс для работы с матрицами 4x4. Оборачивает платформенные вызовы.

**Публичные методы:**
- `static float[] I()` – возвращает единичную матрицу.
- `static void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ)` – создаёт матрицу вида камеры.
- `static void orthoM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far)` – ортографическая проекция.
- `static void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far)` – перспективная проекция.
- `static void multiplyMM(float[] result, int resultOffset, float[] lhs, int lhsOffset, float[] rhs, int rhsOffset)` – умножение матриц.
- `static void translateM(float[] m, int mOffset, float x, float y, float z)` – сдвиг матрицы.
- `static void rotateM(float[] m, int mOffset, float a, float x, float y, float z)` – поворот матрицы (угол в градусах, ось (x,y,z)).
- `static void scaleM(float[] m, int mOffset, float x, float y, float z)` – масштабирование матрицы.
- `static void setIdentityM(float[] sm, int smOffset)` – обнуление матрицы (в единичную).
- `static boolean invertM(float[] mInv, int mInvOffset, float[] m, int mOffset)` – обращение матрицы.
- `static void transposeM(float[] mTrans, int mTransOffset, float[] m, int mOffset)` – транспонирование.
- `static void multiplyMV(float[] resultVec, int resultVecOffset, float[] lhsMat, int lhsMatOffset, float[] rhsVec, int rhsVecOffset)` – умножение матрицы на вектор.
- `static void applyMatrix(float[] mat)` – применяет матрицу к текущему контексту (умножает модельную матрицу).

### Section
Представляет отрезок в 3D. Задаётся базовой точкой и вектором направления.

**Конструкторы:**
- `Section(PVector A, PVector B)` – по двум точкам.
- `Section(Vec3 A, Vec3 B)`

**Публичные методы:**
- `static Section createSectionByBaseAndDirection(Vec3 a, Vec3 b)`
- `static Section createSectionByBaseAndDirection(PVector a, PVector b)`
- `PVector getDirectionVector()`, `PVector getBaseVector()`, `PVector getSecond()`
- `PVector findCross(Section n)` – ищет пересечение двух отрезков в плоскости XY. Возвращает `null`, если нет пересечения или вне отрезков.

---

## 3. Камера и проекция

### Camera
Контейнер для настроек камеры и проекции. Применяет их в OpenGL.

**Конструкторы:**
- `Camera()` – размеры равны размеру экрана.
- `Camera(float x, float y)` – задание размеров области отображения.

**Публичные методы:**
- `void resetFor3d()` – сбрасывает настройки для 3D (перспектива, позиция камеры (0,0,7), центр (0,0,0)).
- `void resetFor2d()` – сбрасывает настройки для 2D (ортографическая проекция, камера смотрит вдоль Z).
- `void apply()` – применяет камеру и проекцию (автоматически определяет режим 3D/2D).
- `void apply(boolean perspectiveEnabled)` – применяет с явным указанием перспективы.
- `void setPerspectiveEnabled(boolean perspectiveEnabled)` – устанавливает флаг перспективы.

### CameraSettings
Хранит параметры вида камеры (eye, center, up). Обычно не используется напрямую, но доступны поля.

**Поля:** `eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ`.

**Публичные методы:**
- `void setPos(PVector pos)`, `void SetUpVector(PVector up)`, `void setCenter(PVector center)`
- `void resetFor3d()`, `void resetFor2d()`

### ProjectionMatrixSettings
Параметры проекции (left, right, bottom, top, near, far). Поля доступны для чтения/записи.

**Публичные методы:**
- `void resetFor3d()`, `void resetFor2d()`

---

## 4. 2D графика и изображения

### AbstractImage
Абстрактный класс, представляющий растровое изображение. Платформенно-зависимая реализация скрыта. Пользователь обычно работает с `PImage`.

**Основные методы (реализуются платформой):**
- `void delete()` – удаляет ресурсы изображения.
- `boolean isLoaded()` – возвращает true, если изображение загружено.
- `void setLoaded(boolean loaded)` – устанавливает флаг загрузки.
- `void background(int r, int g, int b, int a)` – заливка фона цветом RGBA.
- `void rect(float x, float y, float w, float h)` – прямоугольник.
- `void roundRect(float x, float y, float w, float h, float rx, float ry)` – прямоугольник со скруглёнными углами.
- `void ellipse(float cx, float cy, float rx, float ry)` – эллипс.
- `void line(float x1, float y1, float x2, float y2)` – линия.
- `void line(Section section)` – линия по отрезку.
- `void fill(int r, int g, int b, int a)` – цвет заливки.
- `void stroke(int r, int g, int b, int a)` – цвет обводки.
- `void strokeWeight(float w)` – толщина обводки.
- `void noStroke()` – отключить обводку.
- `void textSize(float size)` – размер шрифта.
- `void textAlign(int align)` – выравнивание текста.
- `void text(String text, float x, float y, boolean upperText)` – вывод текста (если upperText=true, переводит в верхний регистр).
- `void setFont(String font)` – загружает шрифт (путь к файлу в assets).
- `void setFont(PFont font)` – устанавливает предварительно загруженный шрифт.
- `void image(AbstractImage img, float x, float y)` – рисует изображение.
- `void image(AbstractImage img, float x, float y, float w, float h)` – с масштабированием.
- `void image(AbstractImage img, float x, float y, float scale)` – с масштабом относительно оригинала.
- `void rotImage(AbstractImage img, float x, float y, float scale, float rotRad)` – повёрнутое изображение (угол в радианах).
- `int getWidth()`, `int getHeight()`
- `Object getBitmap()` – возвращает платформенный объект Bitmap.
- `void createBitmap(int width, int height)` – создаёт пустое растровое изображение.
- `void textAlign(TextAlign align)` – выравнивание текста (LEFT, CENTER, RIGHT).
- `float getTextWidth(String s)` – ширина текста в пикселях с учётом текущего шрифта и размера.
- `float getTextHeight(String s)` – высота текста в пикселях (учитывает переносы строк).
- `void setAntiAlias(boolean b)` – включает/отключает сглаживание примитивов и текста.
- `void drawSector(float cx, float cy, float radius, float startAngle, float sweepAngle, boolean fill)` – рисует сектор круга (углы в градусах). Если `fill` = true, заливает цветом заливки, иначе только обводка (если активна).
- `void clear()` – очищает содержимое изображения, делая его полностью прозрачным.

### PImage
Основной класс для работы с 2D графикой. Оборачивает `AbstractImage` и предоставляет удобные перегрузки.

**Конструкторы:**
- `PImage()` – создаёт пустое изображение (размер задаётся позже через `createBitmap`).
- `PImage(float width, float height)` – создаёт изображение заданного размера.

**Публичные методы:**
- **fill** – устанавливает цвет заливки. Перегрузки: `fill(float color)`, `fill(float r, float g, float b)`, `fill(Vec3 color)`, `fill(float r, float g, float b, float a)`.
- **background** – заливает фон. Аналогичные перегрузки.
- `void text(float text, float x, float y)` – вывод числа как текст.
- `void textAlign(TextAlign align)` – выравнивание текста (LEFT, CENTER, RIGHT).
- `void setUpperText(boolean upperText)` – если true, текст автоматически переводится в верхний регистр.
- `void delete()`, `boolean isLoaded()`, `void setLoaded(boolean v)` – управление ресурсами.
- `void rect(float x, float y, float w, float h)` – прямоугольник.
- `void roundRect(float x, float y, float w, float h, float rx, float ry)` – прямоугольник со скруглёнными углами.
- `void ellipse(float x, float y, float rx, float ry)` – эллипс.
- `void stroke(float s)` – цвет обводки (серый).
- `void stroke(float r, float g, float b)` – цвет обводки RGBA (без альфа-канала, непрозрачный).
- `void stroke(float r, float g, float b, float a)` – цвет обводки RGBA.
- `void strokeWeight(float w)` – толщина обводки.
- `void noStroke()` – отключить обводку.
- `void line(float x1, float y1, float x2, float y2)` – линия.
- `void line(Section s)` – линия по отрезку.
- `void textSize(float s)` – размер шрифта.
- `void text(String s, float x, float y)` – вывод текста (с учётом флага upperText).
- `void image(PImage img, float x, float y)` – рисует изображение в оригинальном размере.
- `void image(PImage img, float x, float y, float w, float h)` – с масштабированием.
- `void image(PImage img, float x, float y, float scale)` – масштаб относительно оригинала.
- `void rotImage(PImage img, float x, float y, float scale, float rot)` – повёрнутое изображение (угол в радианах).
- `int getWidth()`, `int getHeight()`
- `Object getBitmap()` – возвращает платформенный объект Bitmap (может быть null).
- `float getTextWidth(String s)` – ширина текста с текущим шрифтом.
- `float getTextHeight(String s)` – высота текста (включая межстрочный интервал для многострочного текста).
- `void setAntiAlias(boolean b)` – управление сглаживанием.
- `void drawSector(float cx, float cy, float radius, float startAngle, float sweepAngle, boolean fill)` – рисование сектора (углы в градусах).
- `void clear()` – очистка изображения до прозрачного фона.
- `void setFont(PFont font)` – установка шрифта из объекта PFont.

### TextAlign
Перечисление: `LEFT`, `CENTER`, `RIGHT`.

---

## 5. Шрифты

### PFont
Класс для загрузки и управления шрифтами, кроссплатформенная обёртка над платформенным шрифтом.

**Конструкторы:**
- `PFont()` – создаёт пустой объект шрифта (требуется вызов `loadAsset`).
- `PFont(AbstractFont impl)` – для внутреннего использования.

**Публичные методы:**
- `void loadAsset(String assetPath)` – загружает шрифт из assets (Android) или из ресурсов JAR (Desktop). Ожидается файл .ttf или .otf.
- `void close()` – освобождает ресурсы шрифта.
- `boolean isLoaded()` – возвращает `true`, если шрифт успешно загружен.
- `Object getPlatformFont()` – возвращает платформенно-зависимый объект шрифта (Typeface для Android, Font для Skija на Desktop).
- `static PFont fromAsset(String assetPath)` – удобный статический метод для быстрой загрузки шрифта из assets.

**Пример использования:**
```java
PFont myFont = PFont.fromAsset("fonts/Roboto-Regular.ttf");
PImage img = new PImage(800, 600);
img.setFont(myFont);
img.textSize(32);
img.text("Hello, World!", 100, 100);
```
## 6. 3D графика и вершинные объекты

### Shape
Загружает 3D модель из OBJ файла (асинхронно) и отрисовывает её с текстурой и опционально normal map.

**Конструкторы:**
- `Shape(String fileName, String textureFileName, GamePageClass page)` – загружает модель из assets и текстуру.
- `Shape(PreLoadedMesh preLoadedMesh, String textureFileName, GamePageClass page)` – использует предварительно загруженную модель.

**Публичные методы:**
- `void addNormalMap(String normalMapFileName)` – добавляет карту нормалей для модели.
- `void prepareAndDraw()` – отрисовывает модель с текущей матрицей трансформации (должна быть задана через `Matrix.applyMatrix`).
- `void redrawNow()` – принудительно перезагружает текстуру.
- `void onRedrawSetup()`, `void setRedrawNeeded(boolean redrawNeeded)`, `boolean isRedrawNeeded()` – управление перезагрузкой.
- `void delete()` – удаляет ресурсы.
- Статический метод `static void loadFacesAsync(String fileName, Function<PreLoadedMesh, Void> callback)` – асинхронная загрузка OBJ.

### Polygon
Класс для создания простого прямоугольного полигона (два треугольника) с динамически генерируемой текстурой (через `Function`).

**Конструкторы:**
- `Polygon(Function<List<Object>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageClass page)`
- `Polygon(Function<List<Object>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageClass page, boolean mipMap)`

параметр ``saveMemory`` отвечает за удаление PImage из памяти после загрузки его в видеопамять. Если не планируется дальнейшая работа с этой картинкой - рекомендуется ставить true

параметр ``mipMap`` активирует генерацию mip-maps при загрузке текстуры в видеопамять.

параметр ```paramSize``` нужен для задания размеров списка параметров, который буден передан в функцию ``redrawFunction``. Механизм передачи параметров устаревает и скоро будет удален из движка, не рекомендуется к использованию.

**Поля:**
- `List<Object> redrawParams` – параметры для функции генерации текстуры.
- `PImage image` – сгенерированное изображение.

**Публичные методы:**
- `void newParamsSize(int paramSize)` – изменяет размер списка параметров.
- `void prepareData(PVector a, PVector b, PVector d)` – задаёт вершины прямоугольника (a, b, d – три угла, четвёртый вычисляется).
- `void prepareData(PVector a, PVector b, PVector d, float texx, float texy, float texa, float texb)` – с указанием координат текстуры.
- `void prepareAndDraw(PVector a, PVector b, PVector c)` – отрисовка с заданными вершинами (три точки, образуют два треугольника).
- `void prepareAndDraw(PVector a, PVector b, float texx, float texy, float teexa, float texb)` – отрисовка с текстурными координатами.
- `void prepareAndDraw(PVector a, PVector b, PVector c, float texx, float texy, float teexa, float texb)` – отрисовка треугольника с текстурой.
- `void redrawNow()` – принудительная перегенерация текстуры.

### SimplePolygon
Наследник `Polygon` с упрощёнными методами для 2D прямоугольников.

**Публичные методы:**
- `void prepareAndDraw(float x, float y, float b, float z)` – квадрат со стороной b в плоскости XY (z – координата).
- `void prepareAndDraw(float rot, float x, float y, float a, float b, float z)` – прямоугольник a x b, повёрнутый на угол rot (радианы) вокруг центра.
- `void prepareAndDraw(float x, float y, float a, float b, float z)` – прямоугольник без поворота.

### SkyBox
Класс для отрисовки небесного куба (skybox). Использует кубическую текстуру из шести изображений.

**Конструктор:**
- `SkyBox(String textureFileName, String res, GamePageClass page)` – `textureFileName` – базовое имя файлов (например "sky/"), `res` – расширение (например "png"). Файлы должны иметь имена: `"right" + res`, `"left" + res`, `"bottom" + res`, `"top" + res`, `"front" + res`, `"back" + res`.

**Публичные методы:**
- `void prepareAndDraw()` – отрисовывает куб.
- `void redrawNow()` – перезагружает текстуры.

### Face
Внутренний класс, представляющий треугольный полигон (вершины, текстурные координаты, нормали, касательные). Обычно не используется напрямую.

### VerticesSet
Интерфейс для всех объектов, которые содержат вершины и могут быть перерисованы. Реализуется `Shape`, `Polygon`, `SkyBox`, `SectionPolygon`.

**Методы:**
- `void onRedrawSetup()` – вызывается при необходимости пересоздать ресурсы.
- `void setRedrawNeeded(boolean redrawNeeded)`
- `boolean isRedrawNeeded()`
- `void onRedraw()` – перегенерировать текстуру/данные.
- `String getCreatorClassName()`
- `void onFrameBegin()` – вызывается в начале каждого кадра.
- `void delete()`

### VertexBuffer
Управляет VBO и VAO для вершинных данных. Обычно используется внутри `Shape` и `Polygon`.

**Конструктор:**
- `VertexBuffer(int vboNum, GamePageClass creator)`

**Методы:**
- `void setDynamicDraw(boolean dynamicDraw)` – если true, данные будут обновляться часто (GL_DYNAMIC_DRAW).
- `void bindVbo(int vboInd)`, `void bindDefaultVbo()`, `void bindVao()`, `void bindDefaultVao()`
- `int getVboAdress(int vboIndex)`
- `void delete()`

### VRAMobject
Абстрактный базовый класс для всех объектов, использующих видеопамять. Автоматически отслеживает страницу-создателя и удаляет ресурсы при смене страницы.

**Публичные методы:**
- `abstract void delete()`
- `abstract void reload()`

**Владение ресурсами страницы:**

- Каждый экземпляр `GamePageClass` получает отдельный стабильный internal ownership token. Ресурсы, `ShaderData` и input listeners привязаны к экземпляру страницы, а не к её Java-классу.
- При `Engine.startNewPage(...)` точный входящий экземпляр сначала становится текущей страницей, затем получает `onInstalled()` и первый `onSurfaceChanged(...)`, после чего переходные registry удаляют объекты исходящего экземпляра, включая переход между двумя экземплярами одного класса. Устаревшие `ShaderData` удаляются до обновления locations и передачи данных при следующем `Shader.apply()`. Объекты входящей страницы, созданные в её конструкторе, `onInstalled()` и `onSurfaceChanged(...)`, сохраняются; индексы оставшихся directed/point/source lights пересчитываются.
- `creator == null` остаётся global ownership: такие VRAM-объекты, shaders, `ShaderData`, touch processors, keyboard listeners и desktop mouse callbacks переживают переходы страниц.
- Публичные конструкторы и прежние class-name поля/getters сохранены для source compatibility. Порядок перехода, context redraw и reload retained-объектов не изменён.

### VerticesShapesManager
Статический менеджер, управляющий перерисовкой всех `VerticesSet`. Вызывается движком автоматически, но может быть полезен при ручной форсированной перерисовке.

**Публичные методы:**
- `static void redrawAllSetup()`
- `static void onFrameBegin()`
- `static void redrawAll()`
- `static void onRedrawSetup()`

---

## 7. Шейдеры и адаптеры

### Shader
Представляет программу шейдера (вершинный + фрагментный, опционально геометрический). Загружается из файлов в assets.

**Конструкторы:**
- `Shader(String vertex, String fragment, GamePageClass page, Adaptor adaptor)`
- `Shader(String vertex, String fragment, String geom, GamePageClass page, Adaptor adaptor)`

**Публичные методы:**
- `void apply()` – активирует этот шейдер.
- `void delete()`
- `Adaptor getAdaptor()`
- `static Shader getActiveShader()` – возвращает текущий активный шейдер.
- `static void updateAllLocations()` – перечитывает locations uniform-переменных.

### Adaptor
Абстрактный класс, связывающий шейдер с вершинными данными. Для каждого типа шейдера пишется свой адаптер. Предопределены: `MainShaderAdaptor`, `LightShaderAdaptor`, `SectionShaderAdaptor`, `SkyBoxShaderAdaptor`.

**Публичные методы (для переопределения):**
- `abstract int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded)`
- `abstract void bindDataLine(PVector a, PVector b, PVector color)`
- `abstract void updateLocations()`
- `abstract int getTransformMatrixLocation()`
- `abstract int getCameraLocation()`
- `abstract int getProjectionLocation()`
- `abstract int getTextureLocation()`
- `abstract int getNormalTextureLocation()`
- `abstract int getNormalMapEnableLocation()`
- `abstract int getCameraPosLlocation()`

### ShaderData
Базовый класс для данных, передаваемых в шейдер (например, источники света, материал). Позволяет автоматически обновлять uniform-переменные при смене страницы.

Экземпляры с ненулевой страницей принадлежат конкретному экземпляру `GamePageClass`; данные прежней страницы не передаются даже при переходе между двумя объектами одного Java-класса. `null` задаёт global ownership. Прежний protected-метод `getCreatorClass()` сохранён для source compatibility.

**Методы (реализуются в наследниках):**
- `protected abstract void getLocations(int programId)`
- `protected abstract void forwardData()`
- `public void forwardNow()` – немедленно отправить данные в активный шейдер.
- `protected abstract void delete()`

---

## 8. Освещение и материалы

### DirectedLight
Направленный источник света. Наследник `ShaderData`. Автоматически добавляется в массив источников.

**Конструктор:**
- `DirectedLight(GamePageClass gamePageClass)`

**Поля:**
- `PVector color` – цвет света.
- `PVector direction` – направление (должно быть нормализовано).
- `float diffuse`, `float specular` – интенсивности.

**Публичные методы:**
- `void deleteLight()` – удаляет этот источник из списка (должен вызываться перед уничтожением объекта).

### Material
Материал объекта (ambient, diffuse, specular, shininess). Наследник `ShaderData`. Не отправляется автоматически, нужно вызывать `apply()`.

**Конструктор:**
- `Material(GamePageClass gamePageClass)`

**Поля:**
- `PVector ambient`, `diffuse`, `specular`
- `float shininess`

**Публичные методы:**
- `void apply()` – отправляет материал в активный шейдер.

### AmbientLight
Глобальное фоновое освещение (ambient light). Наследник `ShaderData`. Отправляется в шейдер автоматически при смене страницы.

**Конструктор:**
- `AmbientLight(GamePageClass gamePageClass)`

**Поля:**
- `PVector color` – цвет фонового освещения (по умолчанию (0,0,0)).

**Особенности:**  
Не требует вызова дополнительных методов – данные автоматически передаются в активный шейдер.

### PointLight
Точечный источник света. Наследник `ShaderData`. Автоматически добавляется в массив точечных источников.

**Конструктор:**
- `PointLight(GamePageClass gamePageClass)`

**Поля:**
- `PVector color` – цвет света.
- `PVector position` – позиция в мировых координатах.
- `float diffuse`, `float specular` – интенсивности диффузной и зеркальной составляющих.
- `float constant`, `float linear`, `float quadratic` – коэффициенты затухания (attenuation).

**Публичные методы:**
- `void deleteLight()` – удаляет этот источник из списка (должен вызываться перед уничтожением объекта).

### SourceLight
Направленный источник света с конусом (прожектор, spotlight). Наследник `ShaderData`.

**Конструктор:**
- `SourceLight(GamePageClass gamePageClass)`

**Поля:**
- `PVector color` – цвет света.
- `PVector position` – позиция источника.
- `PVector direction` – направление центра луча (должно быть нормализовано).
- `float diffuse`, `float specular` – интенсивности.
- `float constant`, `float linear`, `float quadratic` – коэффициенты затухания.
- `float cutOff` – косинус угла внутреннего конуса (полная яркость).
- `float outerCutOff` – косинус угла внешнего конуса (граница спада).

**Публичные методы:**
- `void deleteLight()` – удаляет этот источник из списка.

### ExpouseSettings
Настройки экспозиции и гамма-коррекции для пост-обработки. Наследник `ShaderData`.

**Конструктор:**
- `ExpouseSettings(GamePageClass gamePageClass)`

**Поля:**
- `float expouse` – экспозиция (по умолчанию 1).
- `float gamma` – гамма (по умолчанию 1).

**Особенности:**  
Данные автоматически передаются в шейдер. Используется совместно с HDR-рендерингом.

### Примечание о встроенных шейдерах для освещения
Движок предоставляет готовые шейдеры для работы с источниками света:
- `light_shader_vertex.glsl`
- `light_shader_fragment.glsl`

Они используют адаптер `LightShaderAdaptor`, который поддерживает карты нормалей (normal mapping), касательные и бикасательные векторы. Для корректной работы необходимо, чтобы в моделях были текстурные координаты и нормали.

---

## 9. Текстуры

### Texture
Базовый класс для 2D текстур.

**Конструкторы:**
- `Texture(GamePageClass creator)`
- `Texture(GamePageClass creator, boolean mipMap)` – если true, генерируются мип-уровни.

**Публичные методы:**
- `void delete()`
- `void reload()`
- `int getId()`
- `boolean hasMinMaps()`

### NormalMap
Специализированная текстура для карт нормалей (наследник `Texture`). Конструктор аналогичен `Texture`.

### CubeMap
Кубическая текстура для skybox. Конструктор: `CubeMap(GamePageClass creator)`.

---

## 10. Анимация

### Animator
Статический класс для создания и управления анимациями объектов `SealObject`.

**Константы для типов трансформации:**
- `SHIFT = 0` – изменение позиции.
- `ROTATION = 1` – изменение поворота.
- `PIVOT_ROTATION = 2` – поворот вокруг заданной точки.

**Константы для функций скорости:**
- `LINEAR = 0` – линейная.
- `SIGMOID = 1` – сигмоида.

**Публичные методы:**
- `static void addAnimation(SealObject target, int tfType, float[] args, int vfType, float duration, float vfa, long st, boolean recurring)` – создаёт анимацию, используя предопределённые типы.
- `static void addAnimation(SealObject target, Function<Animation, float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st, boolean recurring)` – более гибкий вариант с произвольными функциями.
- `static void freezeAnimations(SealObject target)` – приостанавливает все анимации объекта.
- `static void unfreezeAnimations(SealObject target)` – возобновляет.
- `static void animate(SealObject target)` – обновляет анимации объекта (обычно вызывается автоматически из `SealObject.prepareAndDraw`).

### Animator.Animation
Внутренний класс, представляющий одну анимацию. Пользователь не создаёт напрямую.

### FC (Function Collection)
Статический класс с предопределёнными функциями для анимации.

**Публичные методы:**
- `static float linear(float[] params)` – линейная функция (параметр params[0]).
- `static float sigmoid(float[] params)` – сигмоида (params[0] – крутизна, params[1] – время).
- `static float[] rotate(Animator.Animation animation)`, `static float[] shift(...)`, `static float[] pivotRotation(...)` – используются внутри аниматора.

---

## 11. Объекты движка

### SealObject
Представляет игровой объект, который может быть анимирован и отрисован через `Shape`.

**Конструктор:**
- `SealObject(Shape shape)`

**Публичные методы:**
- `void animMotion(float x, float y, float z, float duration, long startTiming, boolean recurring)` – анимация перемещения (вектор скорости в единицах в секунду? фактически дельты за всю анимацию).
- `void animRotation(float x, float y, float z, float duration, long startTiming, boolean recurring)` – анимация поворота (углы в градусах).
- `void animPivotRotation(float x, float y, float z, float vx, float vy, float vz, float duration, long startTiming, boolean recurring)` – поворот вокруг точки (x,y,z) с осью (vx,vy,vz).
- `void stopAnimations()` – замораживает анимации.
- `void continueAnimations()` – возобновляет.
- `void setObjScale(float scale)` – устанавливает масштаб объекта.
- `void prepareAndDraw()` – применяет текущую трансформацию (позиция, поворот, масштаб) и отрисовывает привязанную `Shape`.

---

## 12. Ввод (сенсорный ввод)

### TouchProcessor
Класс для обработки касаний (нажатий, перемещений, отпусканий). Позволяет привязать логику к области экрана.

**Конструктор:**
- `TouchProcessor(Function<TouchPoint, Boolean> checkHitboxCallback, Function<TouchPoint, Void> touchStartedCallback, Function<TouchPoint, Void> touchMovedCallback, Function<TouchPoint, Void> touchEndedCallback, GamePageClass creatorPage)`

**Параметры:**
- `checkHitboxCallback` – вызывается при начале касания; должен вернуть true, если этот процессор должен захватить касание.
- `touchStartedCallback` – вызывается после захвата касания.
- `touchMovedCallback` – вызывается при движении пальца.
- `touchEndedCallback` – вызывается при отпускании.

**Публичные методы:**
- `void setPriority(int priority)` – чем выше приоритет, тем раньше процессор получает шанс захватить касание.
- `void block()`, `void unblock()` – временное отключение.
- `long getDuration()` – длительность текущего касания в миллисекундах (или -1, если нет касания).
- `boolean getTouchAlive()` – true, если касание активно.
- `void terminate()` – принудительно завершает обработку касания.
- `void delete()` – удаляет процессор.
- `static void setLeftButtonProcessor(Function<MousePoint, Void> processor, GamePageClass creatorPage)` – регистрирует desktop-only обработчик нажатия левой кнопки мыши для страницы.
- `static void setRightButtonProcessor(Function<MousePoint, Void> processor, GamePageClass creatorPage)` – регистрирует desktop-only обработчик нажатия правой кнопки мыши для страницы.
- `static void setMouseMovedProcessor(Function<MousePoint, Void> processor, GamePageClass creatorPage)` – регистрирует desktop-only обработчик движения мыши для страницы.
- `static void setMouseWheelProcessor(Function<MouseWheelData, Void> processor, GamePageClass creatorPage)` – регистрирует desktop-only обработчик колеса мыши для страницы.
- `static boolean getLeftButtonDown()`- desktop only, true если левая кнопка мыши зажата, в остальных случаях, включая android, false
- `static boolean getRightButtonDown()`- desktop only, true если правая кнопка мыши зажата, в остальных случаях, включая android, false
- `static void disableAll() / enableAll()` - выключает/включает все обработчики
- `static void disablePriorities(int min, int max) / enablePriorities(int min, int max)` - выключает / включает все обработчики в диапазоне min - max включительно


**Семантика mouse callbacks:**
- Для каждого типа обработчика хранится ровно один callback на страницу.
- Повторный вызов того же setter для той же страницы перезаписывает предыдущий callback.
- `creatorPage == null` регистрирует global handler, который переживает переходы и используется, если для текущей страницы нет собственного callback.
- Хранилище разделено по типам:
  - page -> left button processor
  - page -> right button processor
  - page -> mouse moved processor
  - page -> mouse wheel processor
- Raw platform mouse events могут приходить много раз за кадр, но движок не строит очередь mouse events.
- Вместо этого движок хранит только latest mouse state в переиспользуемых полях/объектах и перезаписывает его при новых platform callbacks.
- User mouse callbacks вызываются не чаще одного раза за кадр, из frame update / render-thread dispatch.
- Из-за этого промежуточные raw mouse positions и wheel events внутри одного кадра могут быть пропущены намеренно. Для этого теста и для engine-style input это корректно.
- Desktop runtime behavior:
  - platform callback только обновляет stored mouse state
  - `setLeftButtonProcessor(...)` вызывается не чаще одного раза за кадр, если с прошлого frame dispatch была зафиксирована левая кнопка (`GLFW_PRESS`)
  - `setRightButtonProcessor(...)` вызывается не чаще одного раза за кадр, если с прошлого frame dispatch была зафиксирована правая кнопка (`GLFW_PRESS`)
  - `setMouseMovedProcessor(...)` вызывается не чаще одного раза за кадр и получает latest mouse coordinates
  - `setMouseWheelProcessor(...)` вызывается не чаще одного раза за кадр и получает latest mouse coordinates плюс накопленный за кадр wheel delta
- Android runtime behavior: эти callbacks никогда не вызываются и не эмулируются через touch/gesture input.
- Для быстрой ручной проверки в `desktop/src/test/java/MouseCallbacksSmokeTestMain.java` добавлена standalone desktop test scene с двумя полигонами: один следует за мышью через latest mouse-move state, второй двигается по Y через once-per-frame wheel delivery. Это debug/smoke test, а не gameplay feature.

### TouchPoint
Простой класс, хранящий координаты касания.

**Поля:**
- `float touchX`, `touchY`

### MousePoint
Переиспользуемый snapshot координат мыши.

**Поля:**
- `float mouseX`, `mouseY`

### MouseWheelData
Переиспользуемый snapshot события колеса мыши.

**Поля:**
- `float mouseX`, `mouseY` – текущие координаты курсора в момент wheel event.
- `float wheelX`, `wheelY` – scroll delta, передаваемая desktop platform layer.

### MyMotionEvent
Интерфейс, абстрагирующий платформенное событие касания. Константы `ACTION_DOWN`, `ACTION_UP`, `ACTION_MOVE`, `ACTION_POINTER_DOWN`, `ACTION_POINTER_UP`. Пользователь не реализует напрямую.

`AndroidMotionEventAdapter` при создании копирует action, action index, IDs и
координаты всех pointers. Он не хранит исходный recyclable `MotionEvent`,
поэтому snapshot можно безопасно передать из UI thread в render thread:

```java
AndroidMotionEventAdapter snapshot = new AndroidMotionEventAdapter(event);
glSurfaceView.queueEvent(() -> TouchProcessor.onTouch(snapshot));
```

---

## 12.1 Ввод (клавиатура)

Клавиатурный ввод реализован через слушатели нажатия/удержания/отпускания клавиш. Коллбэки буферизуются и выполняются в основном (рендер) потоке.

### KeyListener (нажатие + удержание)

**Конструкторы:**
- `KeyListener(String key, Function<String, Void> keyPressedCallback, GamePageClass creatorPage)` – привязка к одной клавише.
- `KeyListener(String[] keys, Function<String, Void> keyPressedCallback, GamePageClass creatorPage)` – привязка к нескольким клавишам (срабатывает на любую из них).

**Дополнительно:**
- `static KeyListener anyKey(Function<String, Void> keyPressedCallback, GamePageClass creatorPage)` – срабатывает на любую клавишу.
- `KeyListener setHoldListener(long timeoutMs, Function<String, Void> keyHoldCallback)` – коллбэк удержания: вызывается один раз, если клавиша удерживается дольше `timeoutMs`.
- `void block()`, `void unblock()`, `void delete()`

### KeyReleasedListener (отпускание)

**Конструкторы:**
- `KeyReleasedListener(String key, Function<String, Void> keyReleasedCallback, GamePageClass creatorPage)`
- `KeyReleasedListener(String[] keys, Function<String, Void> keyReleasedCallback, GamePageClass creatorPage)`

**Дополнительно:**
- `static KeyReleasedListener anyKey(Function<String, Void> keyReleasedCallback, GamePageClass creatorPage)`
- `void block()`, `void unblock()`, `void delete()`

### KeyboardProcessor (состояние клавиш)

**Публичные методы:**
- `static boolean isKeyPressed(String key)` – нажата ли клавиша сейчас.
- `static int getKeysPressedNumber()` – сколько клавиш нажато сейчас.
- `static List<String> getKeyPresedList()` – список нажатых клавиш сейчас (имена нормализованы, uppercase).

### Desktop Mouse Control

Mouse control exposed through `Engine` and implemented only on desktop.

**Публичные методы:**
- `void disableMouseCursor()`
- `void enableMouseCursor()`
- `void setMousePosition(float x, float y)` – координаты внутри окна в пикселях.

**Поведение по платформам:**
- Desktop: использует окно GLFW, не меняя существующий keyboard/touch pipeline.
- Android: все методы безопасно ничего не делают.

### KeyComboListener (комбинации клавиш)

Слушатель, у которого коллбэк вызывается только если **все** указанные клавиши нажаты одновременно (порядок нажатия не важен). Коллбэк вызывается **один раз** на активацию комбинации; после отпускания любой клавиши комбинация сбрасывается и может сработать снова.

**Конструкторы:**
- `KeyComboListener(String[] keys, Function<String, Void> comboPressedCallback, GamePageClass creatorPage)`
- `KeyComboListener(String key1, String key2, Function<String, Void> comboPressedCallback, GamePageClass creatorPage)`

**Аргумент коллбэка:** строка с названием комбинации (нормализовано, `KEY1+KEY2+...`, отсортировано по имени).
- `void block()`, `void unblock()`, `void delete()`

---

## 13. Аудио

### AudioPlayer
Интерфейс для воспроизведения звука и музыки. Реализуется платформой.

**Публичные методы:**
- `void playMusic(String path, boolean loop)`
- `void stopMusic()`, `void pauseMusic()`, `void resume()` - управление воспроизведением музыки (resume = продолжить после паузы)
- `void playSound(String path)` – проиграть одноразовый звук (SFX), не влияя на состояние музыки
- `void setVolume(float volume)` – устанавливает громкость музыки и звуков (диапазон 0.0 – 1.0).
- `float getVolume()` – возвращает текущую громкость.

**Примечания по формату (desktop):**
- Для коротких звуковых эффектов рекомендуется `WAV` (наиболее предсказуемо для SFX).
- `MP3` поддерживается; но для очень коротких эффектов `WAV` обычно надежнее (задержка/паддинг кодека MP3).

**Примечания по Android SFX:**
- Для одноразовых звуков используется `SoundPool`, который **грузит сэмплы асинхронно**. Поэтому корректная реализация должна запускать `play()` после `OnLoadComplete`.
- Для `MP3` SFX на некоторых устройствах `SoundPool` работает нестабильно; в движке есть fallback на короткоживущий `MediaPlayer` для `*.mp3` SFX.
- Если используете `AssetManager.openFd(...)` (как в реализации движка), убедитесь что `wav/ogg/mp3` не сжимаются при упаковке (см. `android/build.gradle` `aaptOptions.noCompress`).

**Быстрая проверка (desktop):**
- Запуск: `desktop/src/test/java/AudioSmokeTestMain.java` (main-класс `AudioSmokeTestMain`)
- Использует ресурсы на classpath: `bsod.mp3`, `test.wav`, и опционально `test.mp3`.

---

## 14. Утилиты и вспомогательные классы

### Utils
Статический класс с математическими и вспомогательными функциями.

**Публичные методы (часто используемые):**
- `static float getX()`, `getY()` – размер экрана.
- `static float getKx()`, `getKy()` – коэффициенты масштабирования (размер "дефолтного" экрана / 1280x720).
- `static void background(int r, int b, int g)` – устанавливает цвет очистки экрана (OpenGL).
- `static float findDrot(float rot, float aimRot)` – минимальное изменение угла для достижения цели (в градусах).
- `static float sq(float a)`, `sqrt(float a)`, `pow(float a, float b)`
- `static float[] contactArray(float[] a, float[] b)` – объединяет два массива.
- `static void delay(long t)` – засыпает поток.
- `static float cutTail(float i, int s)` – округляет до s знаков после запятой.
- `static float random(float a, float b)` – случайное число в диапазоне.
- `static int parseInt(float i)`, `parseInt(String i)`, `parseInt(boolean i)`
- `static float degrees(float a)`, `radians(float a)`, `sin`, `cos`, `tg`, `atan`, `atan2`
- `static float min`, `max`, `abs`
- `static float map(float val, float vstart, float vstop, float ostart, float ostop)` – линейное преобразование.
- `static long millis()` – время с момента запуска приложения (с учётом пауз).
- `static void freezeMillis()`, `unfreezeMillis()` – приостановка/возобновление таймера.
- `static float getTimeK()` – коэффициент времени (120 / текущий FPS) для физики.

### FileUtils
Утилита для работы с файлами из assets.

Для runtime user files используйте `Engine.loadTextFile(...)`, `Engine.saveTextFile(...)`, `Engine.fileExists(...)`, `Engine.folderExists(...)`, `Engine.createFolder(...)`.

**Конструктор:**
- `FileUtils()`

**Публичные методы:**
- `String readFileFromAssets(String fileName)` – читает текстовый файл.
- `static PImage loadImage(InputStream inputStream)`
- `static PImage loadImage(String fileName)`

### Platform
Перечисление: `DESKTOP`, `MOBILE`.

### SealAssetManager
Интерфейс для загрузки ресурсов из assets. Реализуется платформой.

**Методы:**
- `InputStream load(String path)`
- `String loadText(String path)`
- `byte[] loadBytes(String path)`

---

## 15. Дополнительные классы

### FrameBuffer
Позволяет рендерить в текстуру (off-screen rendering). Может использоваться для пост-эффектов.

**Конструктор:**
- `FrameBuffer(int width, int height, GamePageClass page)`

**Публичные методы:**
- `void drawTexture(PVector a, PVector b, PVector d)` – отрисовывает содержимое буфера как текстуру на прямоугольник.
- `int getFrameBuffer()`, `int getDepth()`, `int getTexture()`, `int getWidth()`, `int getHeight()`
- `void resize(int width, int height)` – переаллоцирует только зависящие от размера framebuffer/texture/depth attachments и сохраняет уже созданный fullscreen-quad VBO.
- `void apply()` – активирует этот буфер для рендеринга.
- `void connectDefaultFrameBuffer()` – переключает обратно на экранный буфер.
- `void delete()` – идемпотентно удаляет attachments и принадлежащий framebuffer fullscreen-quad VBO.

### SectionPolygon
Утилитный класс для отрисовки отрезков (линий) через шейдер.

**Конструктор:**
- `SectionPolygon(GamePageClass page)`

**Публичные методы:**
- `void draw(Section section)` – рисует отрезок.
- `void setColor(PVector color)` – устанавливает цвет линии.

---

## 16. Отладка

### Debugger
Статический класс, предоставляющий встроенный графический интерфейс для отладки значений и отображения FPS. Позволяет во время работы приложения изменять числовые параметры (например, скорость, силу света) через ползунки.

**Публичные методы:**
- `static void debuggerInit()` – инициализирует отладчик (вызывается автоматически при первом использовании, но можно вызвать вручную для ранней настройки).
- `static DebugValueFloat addDebugValueFloat(float min, float max, String name)` – создаёт отлаживаемую переменную с плавающей точкой. При повторном вызове с тем же именем возвращает существующий объект.
- `static void draw()` – отрисовывает интерфейс отладчика (вызывается движком автоматически).
- `static void onResChange(int x, int y)` – обновляет размеры интерфейса при изменении экрана.
- `static void setEnabled(boolean debuggerEnabled)` – включает/выключает отладчик.
- `static TouchProcessor getMainPageTouchProcessor()` – возвращает обработчик касаний для управления отладчиком (используется движком).
- `static int getPage()` – возвращает номер текущей страницы меню отладчика (0 – свёрнуто, 1 и выше – открыто меню).

**Примечание:**  
Отладчик автоматически показывает FPS в левом верхнем углу. При касании этой области открывается меню, где можно изменять значения, добавленные через `addDebugValueFloat`. Для выхода из меню нужно коснуться крестика внизу экрана.

### Axes
Класс для отрисовки осей координат (X – красный, Y – зелёный, Z – синий) в 3D-пространстве. Используется для отладки сцены.

**Конструктор:**
- `Axes(GamePageClass gamePageClass)`

**Публичные методы:**
- `void drawAxes(float limit, float step, float tickSize, float[] matrix, Camera camera)` – отрисовывает оси.
    - `limit` – максимальная длина каждой оси (от -limit до limit).
    - `step` – шаг между засечками на осях.
    - `tickSize` – длина засечек (перпендикулярно оси).
    - `matrix` – матрица трансформации (может быть null, тогда используется единичная).
    - `camera` – камера, через которую выполняется рендеринг.

**Особенности:**  
Для отрисовки используется встроенный шейдер `line_vertex_engine.glsl` и `line_fragment_engine.glsl`. Оси рисуются с учётом переданной матрицы, что позволяет отображать их в локальной системе координат объекта.

### DebugValueFloat
Класс, представляющий отлаживаемое значение с плавающей точкой. Экземпляры создаются только через `Debugger.addDebugValueFloat()`.

**Поля:**
- `float value` – текущее значение (можно читать и изменять в коде игры).
- `protected float min`, `max` – границы ползунка.
- `protected String name` – отображаемое имя.

**Примечание:**  
Значение `value` автоматически синхронизируется с ползунком в интерфейсе отладчика.

---

## Заключение

Данная документация покрывает основные классы движка Seal Engine 3-M. Для создания игры необходимо:

1. Реализовать свою страницу, унаследовав `GamePageClass`.
2. Загрузить и скомпилировать шейдеры, передать в них матрицу преобразования.
3. В `onSurfaceChanged` инициализировать камеру (`Camera`), загрузить ресурсы. Передать камеру в шейдер.
4. В `draw` вызывать `camera.apply()`, отрисовывать 2D/3D объекты.
5. Использовать `TouchProcessor` для обработки ввода.
6. Для 3D моделей – загружать `Shape` и отрисовывать через `SealObject` или напрямую с применением матриц через `Matrix.applyMatrix`.
7. Для 2D интерфейса – использовать `PImage` (рисование примитивов, текста, изображений).
8. Для анимации – создавать анимации через `Animator`.

При смене страницы движок автоматически удаляет ресурсы, созданные на предыдущей странице (через `VRAMobject`).
```

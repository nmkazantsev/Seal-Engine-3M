# Seal Engine 3-M Release v3.2.3 Notes

## Overview

This update adds a runtime text-file API to the engine, introduces desktop mouse capture/cursor control, extends `TouchProcessor` with desktop mouse callbacks, and adds a small desktop smoke-test scene for manual verification. The platform behavior is now documented more explicitly, especially for Android runtime storage and the once-per-frame mouse input model.

## Filesystem And Storage

- Added new engine-level runtime filesystem methods on `Engine`:
  - `loadTextFile(String path)`
  - `saveTextFile(String path, String text)`
  - `fileExists(String path)`
  - `folderExists(String path)`
  - `createFolder(String path)`
- These methods are for runtime-generated files only. They do not use packaged assets or classpath resources.
- All text I/O uses UTF-8.
- Relative and absolute paths follow one shared rule set:
  - absolute paths are used directly
  - relative paths are resolved against a platform-specific runtime root
  - normalized relative paths cannot escape that root
- `fileExists(...)` returns `true` only for regular files.
- `folderExists(...)` returns `true` only for directories.
- `createFolder(...)` creates nested directories recursively.
- `saveTextFile(...)` does not create missing parent folders automatically.

### Platform Notes

- Desktop relative paths resolve from the current working directory (`System.getProperty("user.dir")`).
- Android relative paths resolve from the app’s internal files directory (`Context.getFilesDir()`), which is a real writable runtime storage location for app-generated files.
- Android runtime file storage is intentionally separate from assets/resources and separate from the desktop working-directory model.

## Desktop Mouse Control

- Added desktop mouse control methods to `Engine`:
  - `disableMouseCursor()`
  - `enableMouseCursor()`
  - `setMousePosition(float x, float y)`
- On desktop, these methods operate on the real GLFW window.
- On Android, these methods remain safe no-ops by design.

## TouchProcessor Desktop Mouse Callbacks

- Added page-scoped desktop mouse callback registration on `TouchProcessor`:
  - `setLeftButtonProcessor(...)`
  - `setRightButtonProcessor(...)`
  - `setMouseWheelProcessor(...)`
  - `setMouseMovedProcessor(...)`
- Each handler type stores exactly one callback per page. Re-registering the same handler type for the same page overwrites the previous callback.
- Android does not dispatch these mouse callbacks at runtime.

### Delivery Model

- Raw desktop platform events update only the latest stored mouse state.
- User mouse callbacks are dispatched at most once per frame from the engine’s frame-processing path.
- Intermediate raw mouse positions may be skipped intentionally if multiple platform events arrive within one frame.
- Wheel input is accumulated within the frame and delivered once per frame with the latest mouse coordinates.


This reduces event churn and avoids per-event callback object allocation for mouse handling.

## Testing And Debug Scene

- Added `desktop/src/test/java/MouseCallbacksSmokeTestMain.java` as a standalone desktop smoke test.
- The scene contains two visible polygons:
  - a wheel-controlled polygon that moves only on the Y axis
  - a mouse-follow polygon that tracks the latest mouse position
- This scene is intended only for manual verification of desktop mouse callback behavior. It is not gameplay code.

## BSOD Screen And Crash Logs

- BSOD support is initialized during engine startup when `LauncherParams.setUseBSOD(true)` is enabled.
- If the user application throws an exception inside engine-managed execution paths, the engine automatically switches to the BSOD screen.
- The BSOD screen shows error details on screen.
- The same error information is also saved automatically to a text crash log.
- Crash log locations:
  - Desktop: `crashes` folder inside the application folder
  - Android: `Android/data/<app>/files/crashes`
- Crash log filenames are text files and include the date and time of the error.


## Notes And Limitations

- Android mouse callbacks remain unsupported by design and are never emulated from touch input.
- Mouse callbacks are delivered at most once per frame.
- Relative paths resolve differently on desktop and Android, but the resolution rule is unified and documented.
- Runtime file APIs are intentionally separate from bundled resource loading.

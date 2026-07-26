# Seal Engine Runtime Debug Observer Implementation Plan

## Global Constraints

- Base all work on commit `00722de8a0d0a52d12fac114c44586c20480eceb`.
- Preserve every existing public default and the production frame/page/input order.
- With no observer and no capture request: no per-frame allocations, no framebuffer readback, no worker, and no diagnostics I/O.
- Keep `core` platform-neutral and Android-compilable; desktop-specific OpenGL code stays in `desktop`.
- Do not change `Engine.pageMillis()`, FPS calculation, or the engine time model.
- Add behavior through tests first and record RED/GREEN evidence.
- Update README contracts in the same commit as each public API change.

## Task 1: Optional runtime observer contract

Introduce a minimal core API for observing a frame without changing the legacy path.

- Add immutable runtime DTOs for frame context, page transition, and runtime failure.
- Add `RuntimeObserver` with default no-op methods: `beforeFrame`, `afterFrame`, `onPageChanged`, and `onFailure`.
- Add an optional observer to `LauncherParams`; the default must be `null`.
- Notify page transitions with old/new page instances without changing transition order.
- Notify failures from page rendering and post-page frame stages, preserving existing BSOD/rethrow behavior.
- Call `beforeFrame` immediately before the existing frame body and `afterFrame` after debugger, queued vertices, touch, and keyboard processing.
- Only create frame DTOs and increment observer frame IDs when an observer is installed.
- Characterization tests must prove the no-observer path retains existing ordering and the observer path reports exact ordering/failures.

Verification:

- `./gradlew :core:test :desktop:test --no-daemon`
- `./gradlew :core:jar :desktop:jar --no-daemon`

## Task 2: On-demand cross-platform frame capture and window settings

Add capture and deterministic desktop-window capabilities behind platform-neutral contracts.

- Add `FrameCaptureSource` and immutable `CapturedFrame` in `core`; RGBA byte order and top-left orientation are part of the contract.
- Extend the platform bridge with capability reporting and on-demand RGBA readback.
- Desktop readback uses the current default framebuffer after `CoreRenderer.draw()` and before swap. It must preserve prior GL pack state and restore it.
- Android readback uses the current default framebuffer from the observer
  `afterFrame` callback on the `GLSurfaceView` GL thread, before
  `CoreRenderer.draw()` returns and before swap. It preserves read framebuffer
  and pack-alignment state, survives surface recreation, and converts
  bottom-left GLES rows to the shared top-left RGBA contract.
- Capture occurs only when observer code explicitly calls the source; never pre-capture each frame.
- Add `LauncherParams` window width, height, maximized, and VSync settings.
- Preserve legacy defaults exactly. Explicit settings must allow a non-maximized 1280x720 window with VSync disabled.
- Add render-thread-only `DesktopLauncher.requestStop()` and `requestWindowSize(width, height)` controls for clean debug shutdown and resize scenarios. They must use GLFW lifecycle operations, reject inactive/wrong-thread calls predictably, and never use `System.exit`.
- Add render-thread-only `DesktopLauncher.requestPage(page)` for synchronous
  coordinator resets without exposing `Engine` or introducing a command queue.
  It shares the active-window/owner-thread boundary and preserves exact
  `PageTransition` instances.
- Use the binary-compatible default-no-op `GamePageClass.onInstalled()` hook
  for application boundaries that must run only after the exact page is
  current. Candidate construction remains lifecycle-neutral.
- Preserve the last positive viewport across transient non-positive surface
  callbacks so reset while minimized cannot initialize page resources at
  `0x0`; test `positive -> 0x0 -> install/draw -> positive`.
- Add tests for defaults, validation, capability behavior, orientation conversion, and no-readback when capture is not requested.

Verification:

- `./gradlew :core:test :desktop:test --no-daemon`
- `./gradlew :core:jar :desktop:jar --no-daemon`
- `./gradlew :android:compileDebugJavaWithJavac --no-daemon`

## Task 3: Instance-safe page resource ownership and diagnostics

Correct same-class page cleanup without changing different-class or global ownership behavior.

- Introduce an internal page generation/identity ownership token.
- Migrate VRAM, Shader, ShaderData/light registries, TouchProcessor, and KeyboardProcessor cleanup to the token while retaining source-compatible constructors/APIs.
- `creator == null` remains global and is not deleted by page transitions.
- Resources created by the incoming page must not be deleted during transition cleanup.
- Existing different-class transition cleanup remains equivalent.
- Same-class transition deletes resources/processors owned by the outgoing instance.
- Add observer-visible resource counters without work when no observer is installed.
- Add characterization tests for null owner, different-class transition, same-class transition, context redraw, incoming-resource preservation, ShaderData forwarding, and indexed light compaction.
- Keep `FrameBuffer` and its lazily-created child `VertexBuffer` under one
  stable page lifecycle: resize reallocates only attachments, context redraw
  regenerates the existing VBO wrapper once, and deletion is idempotent.
- Document the corrected instance ownership lifecycle and compatibility boundary.

Verification:

- `./gradlew :core:test :desktop:test --no-daemon`
- `./gradlew :core:jar :desktop:jar --no-daemon`
- `./gradlew :android:compileDebugJavaWithJavac --no-daemon`
- Compare no-observer allocation/frame benchmark to the `00722de8` baseline; any stable regression blocks completion.

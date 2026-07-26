package com.seal.gl_engine.platform;

final class AndroidSessionRegistry {
    private AndroidProcessSession session;

    synchronized AndroidProcessSession acquire(
            AndroidLaunchSettings settings,
            SessionFactory factory
    ) {
        if (session == null) {
            session = factory.create(settings);
        }
        return session;
    }

    interface SessionFactory {
        AndroidProcessSession create(AndroidLaunchSettings settings);
    }
}

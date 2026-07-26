package com.nikitos;

/**
 * Engine-internal bridge for page-scoped registry ownership.
 */
public final class PageOwnership {
    private PageOwnership() {
    }

    public static Object tokenOf(GamePageClass page) {
        return page == null ? null : page.getResourceOwnershipToken();
    }

    public static Object currentToken() {
        try {
            return CoreRenderer.engine == null
                    ? null
                    : tokenOf(CoreRenderer.engine.getGamePage());
        } catch (Throwable ignored) {
            return null;
        }
    }
}

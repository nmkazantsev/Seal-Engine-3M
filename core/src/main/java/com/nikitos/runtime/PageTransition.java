package com.nikitos.runtime;

import com.nikitos.GamePageClass;

public final class PageTransition {
    private final GamePageClass previousPage;
    private final GamePageClass newPage;

    public PageTransition(GamePageClass previousPage, GamePageClass newPage) {
        this.previousPage = previousPage;
        this.newPage = newPage;
    }

    public GamePageClass getPreviousPage() {
        return previousPage;
    }

    public GamePageClass getNewPage() {
        return newPage;
    }
}

package org.microcol;

import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.gui.screen.game.gamepanel.CursorServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * This module define class that could be replaced in test by mock
 * implementations.
 * <p>
 * AT CI server some feature like sound are not available. Because of that some
 * game features have to be disabled.
 * </p>
 */
public class ClassesForMocking extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileSelectingService.class).in(Singleton.class);
        bind(CursorService.class).to(CursorServiceImpl.class).in(Singleton.class);
    }

}

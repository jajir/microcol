package org.microcol;

import org.microcol.gui.FileSelectingService;
import org.microcol.gui.gamepanel.CursorService;
import org.microcol.gui.gamepanel.CursorServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ClassesForMocking extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileSelectingService.class).in(Singleton.class);
        bind(CursorService.class).to(CursorServiceImpl.class).in(Singleton.class);
    }

}

package org.microcol;

import org.microcol.gui.FileSelectingService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ClassesForMocking extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileSelectingService.class).in(Singleton.class);
    }

}

package org.microcol.gui.image;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Separate module for images handling.
 * <p>
 * Main reason is that image provider class have to be called manually.
 * </p>
 */
public final class ModuleImages extends AbstractModule {

    protected void configure() {
        bind(ImageProvider.class).in(Singleton.class);
    }

}

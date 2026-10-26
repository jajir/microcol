package org.microcol.gui.image;

import com.google.inject.Inject;

public final class GrassCoastMapGenerator extends AbstractCoastMapGenerator {

    @Inject
    GrassCoastMapGenerator(final ImageProvider imageProvider) {
        super(imageProvider);
    }

    @Override
    public String getPrefix() {
        return "grass-";
    }

    @Override
    public boolean isVoid(final InfoHolder infoHolder) {
        return infoHolder.tt().isSea();
    }

    @Override
    public boolean isMass(final InfoHolder infoHolder) {
        return infoHolder.tt().isLand();
    }

    @Override
    public boolean skipp(final InfoHolder infoHolder) {
        return false;
    }

}

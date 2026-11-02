package org.akropia.gui.image;

public final class GrassCoastImageLoader extends AbstractCoastImageLoader {

    @Override
    String getTypePrefix() {
        return "grass-";
    }

    @Override
    String getBackgroundRow() {
        return "1";
    }

}

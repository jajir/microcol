package org.microcol.gui.image;

public final class IceCoastImageLoader extends AbstractCoastImageLoader {

    @Override
    String getTypePrefix() {
        return "ice-";
    }

    @Override
    String getBackgroundRow() {
        return "2";
    }

}

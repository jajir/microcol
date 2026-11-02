package org.akropia.gui.image;

public final class HiddenCoastImageLoader extends AbstractCoastImageLoader {

    @Override
    String getTypePrefix() {
        return "hidden-";
    }

    @Override
    String getBackgroundRow() {
        return "3";
    }

}

package org.microcol.gui.image;

public class IceCoastImageLoader extends AbstractCoastImageLoader {

    @Override
    String getTypePrefix() {
        return "ice-";
    }

    @Override
    String getBackgroundRow() {
        return "2";
    }

}

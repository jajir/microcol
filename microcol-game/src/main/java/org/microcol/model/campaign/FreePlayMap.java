package org.microcol.model.campaign;

/** Bundled worlds available when creating a free-play game. */
public enum FreePlayMap {
    ORIGINAL("/maps/free-play.microcol"),
    TWO_ISLANDS("/maps/free-play-two-islands.microcol"),
    RIVAL_EXPEDITION("/maps/free-play-rival-expedition.microcol");

    private final String resource;

    FreePlayMap(final String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}

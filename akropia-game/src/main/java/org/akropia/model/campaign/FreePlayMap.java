package org.akropia.model.campaign;

/** Bundled worlds available when creating a free-play game. */
public enum FreePlayMap {
    ORIGINAL("/maps/original-world.akropia"),
    TWO_ISLANDS("/maps/free-play-two-islands.akropia"),
    RIVAL_EXPEDITION("/maps/free-play-rival-expedition.akropia"),
    REFERENCE("/maps/free-play.akropia"),
    /**
     * Authored geographic world. Its resource is only the free-play template;
     * the geography comes from
     * {@link org.akropia.model.generation.NorthAmericaWorld}.
     */
    NORTH_AMERICA("/maps/free-play.akropia");

    private final String resource;

    FreePlayMap(final String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    /**
     * @return {@code true} when every European nation may be the player; other
     *         authored maps are fixed to the Dutch
     */
    public boolean isNationSelectable() {
        return this == REFERENCE || this == NORTH_AMERICA;
    }
}

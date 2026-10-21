package org.microcol.model.generation;

/**
 * Climate of a generated New World. It shifts the latitude bands that decide
 * the tundra and arctic squares: the cold climate pushes them toward the
 * equator and the warm climate pulls them toward the poles.
 */
public enum GeneratedClimate {

    COLD(0.25), TEMPERATE(0), WARM(-0.25);

    private final double coldShift;

    GeneratedClimate(final double coldShift) {
        this.coldShift = coldShift;
    }

    /**
     * @return amount added to the normalised latitude when judging coldness
     */
    double getColdShift() {
        return coldShift;
    }

}

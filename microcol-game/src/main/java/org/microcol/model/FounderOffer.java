package org.microcol.model;

/** A selection bound to one player's frozen candidate generation. */
public final class FounderOffer {
    private final Player owner;
    private final Founder founder;
    private final int generation;

    FounderOffer(final Player owner, final Founder founder,
            final int generation) {
        this.owner = owner;
        this.founder = founder;
        this.generation = generation;
    }

    public Founder getFounder() {
        return founder;
    }

    boolean belongsTo(final Player player, final int currentGeneration) {
        return owner == player && generation == currentGeneration;
    }
}

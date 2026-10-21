package org.microcol.model.store;

import org.microcol.model.generation.GeneratedClimate;
import org.microcol.model.generation.GeneratedLandShare;

/**
 * Saved settings that produced a generated New World. Bundled worlds have no
 * such entry. When it is present, every field is required and nothing is
 * regenerated on Continue.
 */
public final class GeneratedWorldPo {

    private Integer seed;

    private GeneratedClimate climate;

    private GeneratedLandShare landShare;

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(final Integer seed) {
        this.seed = seed;
    }

    public GeneratedClimate getClimate() {
        return climate;
    }

    public void setClimate(final GeneratedClimate climate) {
        this.climate = climate;
    }

    public GeneratedLandShare getLandShare() {
        return landShare;
    }

    public void setLandShare(final GeneratedLandShare landShare) {
        this.landShare = landShare;
    }

    /**
     * @return {@code true} when the seed, climate and land share are all
     *         present
     */
    public boolean isComplete() {
        return seed != null && climate != null && landShare != null;
    }

}

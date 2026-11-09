package org.akropia.model.store;

import org.akropia.model.generation.GeneratedClimate;
import org.akropia.model.generation.GeneratedLandShare;

/**
 * Saved settings that produced a generated New World. Bundled worlds have no
 * such entry. When it is present, every field is required and nothing is
 * regenerated on Continue.
 */
public final class GeneratedWorldPo {

    private Integer seed;
    private String textSeed;
    private org.akropia.model.generation.GeneratedMapSize size;
    private org.akropia.model.generation.GeneratedLandLayout layout;
    private Integer version;

    public String getTextSeed() {
        return textSeed;
    }

    public void setTextSeed(String value) {
        textSeed = value;
    }

    public org.akropia.model.generation.GeneratedMapSize getSize() {
        return size;
    }

    public void setSize(org.akropia.model.generation.GeneratedMapSize value) {
        size = value;
    }

    public org.akropia.model.generation.GeneratedLandLayout getLayout() {
        return layout;
    }

    public void setLayout(
            org.akropia.model.generation.GeneratedLandLayout value) {
        layout = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer value) {
        version = value;
    }

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
        if (seed == null || climate == null || landShare == null
                || version == null) {
            return false;
        }
        if (version == 1) {
            return textSeed == null && size == null && layout == null;
        }
        if (version != 2 || textSeed == null || size == null
                || layout == null) {
            return false;
        }
        try {
            return seed == org.akropia.model.generation.GeneratedWorldSettings
                    .seedNumber(textSeed);
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

}

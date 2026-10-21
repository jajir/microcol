package org.microcol.model.store;

import java.util.List;

import org.microcol.model.EuropeanNation;

/**
 * Trade of one settlement with one European nation. The goods last received
 * from a carrier is null until the first delivery; the expeditions list holds
 * only expeditions open at the moment of saving, and is empty when none is.
 */
public class NativeTradePo {

    private EuropeanNation european;

    /** Goods name the village received most recently, or null. */
    private String lastReceivedGoods;

    /** Open expeditions of carriers, required; empty when none. */
    private List<NativeExpeditionPo> expeditions;

    /**
     * Transaction revision of this trade (R-0160); null or absent means zero,
     * the revision before any expedition, haggle or sale.
     */
    private Integer revision;

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(final Integer revision) {
        this.revision = revision;
    }

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    public String getLastReceivedGoods() {
        return lastReceivedGoods;
    }

    public void setLastReceivedGoods(final String lastReceivedGoods) {
        this.lastReceivedGoods = lastReceivedGoods;
    }

    public List<NativeExpeditionPo> getExpeditions() {
        return expeditions;
    }

    public void setExpeditions(final List<NativeExpeditionPo> expeditions) {
        this.expeditions = expeditions;
    }

}

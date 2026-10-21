package org.microcol.model.store;

import java.util.ArrayList;

import org.microcol.model.NativeNation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerPo {

    private String name;

    private boolean computer;

    private int gold;

    private boolean declaredIndependence;

    private String whosKingThisPlayerIs;

    /**
     * Native nation this participant stands for. Absent for European players
     * and Crowns; a native participant is saved with its explicit nation.
     */
    private NativeNation nativeNation;

    public NativeNation getNativeNation() {
        return nativeNation;
    }

    public void setNativeNation(final NativeNation nativeNation) {
        this.nativeNation = nativeNation;
    }

    private Map<String, Object> extraData = new HashMap<>();

    private VisibilityPo visible;

    private TaxPo tax;

    private RecruitmentPo recruitment;

    private CongressPo congress;

    private List<ColonySightingPo> colonySightings = new ArrayList<>();

    /**
     * @return colonies this player remembers from actual observation
     */
    public List<ColonySightingPo> getColonySightings() {
        return colonySightings;
    }

    public void setColonySightings(
            final List<ColonySightingPo> colonySightings) {
        this.colonySightings = colonySightings;
    }

    public CongressPo getCongress() {
        return congress;
    }

    public void setCongress(final CongressPo congress) {
        this.congress = congress;
    }

    private Map<String, Integer> unitPurchaseCounts = new HashMap<>();

    public Map<String, Integer> getUnitPurchaseCounts() {
        return unitPurchaseCounts;
    }

    public void setUnitPurchaseCounts(
            final Map<String, Integer> unitPurchaseCounts) {
        this.unitPurchaseCounts = unitPurchaseCounts;
    }

    /** Active supply cycle of a rival colony; null when it has none. */
    private RivalTradeObjectivePo rivalTradeObjective;

    public RivalTradeObjectivePo getRivalTradeObjective() {
        return rivalTradeObjective;
    }

    public void setRivalTradeObjective(
            final RivalTradeObjectivePo rivalTradeObjective) {
        this.rivalTradeObjective = rivalTradeObjective;
    }

    public RecruitmentPo getRecruitment() {
        return recruitment;
    }

    public void setRecruitment(final RecruitmentPo recruitment) {
        this.recruitment = recruitment;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the computer
     */
    public boolean isComputer() {
        return computer;
    }

    /**
     * @param computer the computer to set
     */
    public void setComputer(boolean computer) {
        this.computer = computer;
    }

    /**
     * @return the gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * @param gold the gold to set
     */
    public void setGold(int gold) {
        this.gold = gold;
    }

    /**
     * @return the declaredIndependence
     */
    public boolean isDeclaredIndependence() {
        return declaredIndependence;
    }

    /**
     * @param declaredIndependence the declaredIndependence to set
     */
    public void setDeclaredIndependence(boolean declaredIndependence) {
        this.declaredIndependence = declaredIndependence;
    }

    /**
     * @return the whosKingThisPlayerIs
     */
    public String getWhosKingThisPlayerIs() {
        return whosKingThisPlayerIs;
    }

    /**
     * @param whosKingThisPlayerIs the whosKingThisPlayerIs to set
     */
    public void setWhosKingThisPlayerIs(String whosKingThisPlayerIs) {
        this.whosKingThisPlayerIs = whosKingThisPlayerIs;
    }

    /**
     * @return the extraData
     */
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    /**
     * @param extraData the extraData to set
     */
    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    /**
     * @return the visibility
     */
    public VisibilityPo getVisible() {
        return visible;
    }

    /**
     * @param visibility the visibility to set
     */
    public void setVisible(VisibilityPo visibility) {
        this.visible = visibility;
    }

    /**
     * @return the colonial tax state; null for a Crown, which has no tax state
     *         of its own
     */
    public TaxPo getTax() {
        return tax;
    }

    public void setTax(final TaxPo tax) {
        this.tax = tax;
    }

}

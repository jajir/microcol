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

    /** Absent in a save without any European contact, which is empty too. */
    private List<String> knownPowers = new ArrayList<>();

    /**
     * @return names of the colonial powers this player has actually seen, in
     *         alphabetical order (R-0168)
     */
    public List<String> getKnownPowers() {
        return knownPowers;
    }

    public void setKnownPowers(final List<String> knownPowers) {
        this.knownPowers = knownPowers;
    }

    public CongressPo getCongress() {
        return congress;
    }

    public void setCongress(final CongressPo congress) {
        this.congress = congress;
    }

    /**
     * Foreign-intervention target of a declared colonial nation; absent before
     * declaration and before its first owner update after it (R-0189).
     */
    private InterventionPo intervention;

    public InterventionPo getIntervention() {
        return intervention;
    }

    public void setIntervention(final InterventionPo intervention) {
        this.intervention = intervention;
    }

    /**
     * Own updates counted before declaration for the peacetime mercenary
     * interval (R-0169); absent in a save without any counted update.
     */
    private Integer mercenaryTurns;

    public Integer getMercenaryTurns() {
        return mercenaryTurns;
    }

    public void setMercenaryTurns(final Integer mercenaryTurns) {
        this.mercenaryTurns = mercenaryTurns;
    }

    /** Rebel updates counted after declaration (R-0169); absent when none. */
    private Integer mercenaryRebelUpdates;

    public Integer getMercenaryRebelUpdates() {
        return mercenaryRebelUpdates;
    }

    public void setMercenaryRebelUpdates(final Integer mercenaryRebelUpdates) {
        this.mercenaryRebelUpdates = mercenaryRebelUpdates;
    }

    /** Pending offer that the owner has not yet answered; absent when none. */
    private MercenaryOfferPo mercenaryOffer;

    public MercenaryOfferPo getMercenaryOffer() {
        return mercenaryOffer;
    }

    public void setMercenaryOffer(final MercenaryOfferPo mercenaryOffer) {
        this.mercenaryOffer = mercenaryOffer;
    }

    /** Paid mercenary groups, pending or delivered (R-0169). */
    private List<MercenaryDeliveryPo> mercenaryDeliveries = new ArrayList<>();

    public List<MercenaryDeliveryPo> getMercenaryDeliveries() {
        return mercenaryDeliveries;
    }

    public void setMercenaryDeliveries(
            final List<MercenaryDeliveryPo> mercenaryDeliveries) {
        this.mercenaryDeliveries = mercenaryDeliveries;
    }

    /** Prewar Crown roster; required for a Crown and absent for colonies. */
    private RoyalRosterPo royalRoster;

    public RoyalRosterPo getRoyalRoster() {
        return royalRoster;
    }

    public void setRoyalRoster(final RoyalRosterPo royalRoster) {
        this.royalRoster = royalRoster;
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

    /**
     * Colony of the last completed supply round trip; null before the first.
     */
    private Integer rivalLastServedColonyId;

    public Integer getRivalLastServedColonyId() {
        return rivalLastServedColonyId;
    }

    public void setRivalLastServedColonyId(
            final Integer rivalLastServedColonyId) {
        this.rivalLastServedColonyId = rivalLastServedColonyId;
    }

    /** Carriers waiting in their colony ports between supply round trips. */
    private List<Integer> rivalParkedCarrierIds = new ArrayList<>();

    public List<Integer> getRivalParkedCarrierIds() {
        return rivalParkedCarrierIds;
    }

    public void setRivalParkedCarrierIds(
            final List<Integer> rivalParkedCarrierIds) {
        this.rivalParkedCarrierIds = rivalParkedCarrierIds;
    }

    /**
     * Colony IDs of a declared rebellion (R-0192); required for a declared
     * colonial nation, empty or absent for every other player.
     */
    private List<Integer> rebellionColonyIds;

    public List<Integer> getRebellionColonyIds() {
        return rebellionColonyIds;
    }

    public void setRebellionColonyIds(final List<Integer> rebellionColonyIds) {
        this.rebellionColonyIds = rebellionColonyIds;
    }

    /**
     * Reason code under which this declared rebellion was lost (R-0192); null
     * while the rebellion holds and for every non-rebel.
     */
    private String rebellionLossReason;

    public String getRebellionLossReason() {
        return rebellionLossReason;
    }

    public void setRebellionLossReason(final String rebellionLossReason) {
        this.rebellionLossReason = rebellionLossReason;
    }

    /**
     * Completion order of this declared rebellion's victory (R-0191); null
     * while the rebellion is not won and for every non-rebel.
     */
    private Integer rebellionVictoryOrder;

    public Integer getRebellionVictoryOrder() {
        return rebellionVictoryOrder;
    }

    public void setRebellionVictoryOrder(final Integer rebellionVictoryOrder) {
        this.rebellionVictoryOrder = rebellionVictoryOrder;
    }

    /**
     * Calendar year of the declaration (R-0196); required for a declared
     * colonial nation and null for every other player.
     */
    private Integer declarationYear;

    public Integer getDeclarationYear() {
        return declarationYear;
    }

    public void setDeclarationYear(final Integer declarationYear) {
        this.declarationYear = declarationYear;
    }

    /**
     * Native settlements this nation destroyed by conquest (R-0196); zero for a
     * nation that destroyed none.
     */
    private int nativeSettlementsDestroyed;

    public int getNativeSettlementsDestroyed() {
        return nativeSettlementsDestroyed;
    }

    public void setNativeSettlementsDestroyed(
            final int nativeSettlementsDestroyed) {
        this.nativeSettlementsDestroyed = nativeSettlementsDestroyed;
    }

    /** Active second-colony expansion of a rival; null when it has none. */
    private RivalExpansionObjectivePo rivalExpansionObjective;

    public RivalExpansionObjectivePo getRivalExpansionObjective() {
        return rivalExpansionObjective;
    }

    public void setRivalExpansionObjective(
            final RivalExpansionObjectivePo rivalExpansionObjective) {
        this.rivalExpansionObjective = rivalExpansionObjective;
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

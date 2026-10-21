package org.microcol.model.store;

import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlementClass;

/**
 * Saved native settlement. Every field is required in the current schema; a
 * missing field is rejected when the model is built, never guessed.
 */
public class NativeSettlementPo {

    private Integer id;

    private NativeNation nation;

    private NativeSettlementClass settlementClass;

    private Boolean capital;

    private Location location;

    private Integer treasury;

    /** Name of the accepted teachable skill, a {@code UnitType} name. */
    private String teachableSkill;

    /** Pupil of the native course, or null when the teaching place is free. */
    private Integer teachingPupilId;

    /** Owner updates the pupil has completed; zero when there is no pupil. */
    private Integer teachingUpdates;

    /**
     * Skill the course grants, a {@code UnitType} name; null without a pupil.
     */
    private String teachingSkill;

    /**
     * Stage of the course, a NativeTeachingCourse.Stage name; null without a
     * pupil.
     */
    private String teachingStage;

    /** Goods offered for sale with their stock; see the getter. */
    private List<NativeGoodsStockPo> offeredGoods;

    /** Goods names the settlement wants to buy; see the getter. */
    private List<String> wantedGoods;

    /** Trade with each European nation; see the getter. */
    private List<NativeTradePo> trade;

    /**
     * Goods bought from carriers with their amounts; null or absent means none
     * has been bought yet.
     */
    private List<NativeGoodsStockPo> receivedGoods;

    /**
     * @return saved stage of the course, or null without a pupil
     */
    public String getTeachingStage() {
        return teachingStage;
    }

    public void setTeachingStage(final String teachingStage) {
        this.teachingStage = teachingStage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public NativeNation getNation() {
        return nation;
    }

    public void setNation(final NativeNation nation) {
        this.nation = nation;
    }

    public NativeSettlementClass getSettlementClass() {
        return settlementClass;
    }

    public void setSettlementClass(
            final NativeSettlementClass settlementClass) {
        this.settlementClass = settlementClass;
    }

    public Boolean getCapital() {
        return capital;
    }

    public void setCapital(final Boolean capital) {
        this.capital = capital;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    /**
     * @return saved name of the teachable skill, required; it must name one of
     *         the accepted native-teachable skills
     */
    public String getTeachableSkill() {
        return teachableSkill;
    }

    public void setTeachableSkill(final String teachableSkill) {
        this.teachableSkill = teachableSkill;
    }

    /**
     * @return saved pupil of the native course, or null when the place is free
     */
    public Integer getTeachingPupilId() {
        return teachingPupilId;
    }

    public void setTeachingPupilId(final Integer teachingPupilId) {
        this.teachingPupilId = teachingPupilId;
    }

    /**
     * @return saved owner updates of the course, required; zero without a pupil
     */
    public Integer getTeachingUpdates() {
        return teachingUpdates;
    }

    public void setTeachingUpdates(final Integer teachingUpdates) {
        this.teachingUpdates = teachingUpdates;
    }

    /**
     * @return saved skill name of the course, or null without a pupil
     */
    public String getTeachingSkill() {
        return teachingSkill;
    }

    public void setTeachingSkill(final String teachingSkill) {
        this.teachingSkill = teachingSkill;
    }

    /**
     * @return treasury in gold, required; it starts at the agreed initial value
     *         and only grows by land payments in this schema
     */
    public Integer getTreasury() {
        return treasury;
    }

    public void setTreasury(final Integer treasury) {
        this.treasury = treasury;
    }

    /**
     * @return saved goods offered for sale with their stock, required; at most
     *         three, empty when the surroundings produce nothing tradable
     */
    public List<NativeGoodsStockPo> getOfferedGoods() {
        return offeredGoods;
    }

    public void setOfferedGoods(final List<NativeGoodsStockPo> offeredGoods) {
        this.offeredGoods = offeredGoods;
    }

    /**
     * @return saved goods names the settlement wants to buy, required; at most
     *         three distinct tradable goods
     */
    public List<String> getWantedGoods() {
        return wantedGoods;
    }

    public void setWantedGoods(final List<String> wantedGoods) {
        this.wantedGoods = wantedGoods;
    }

    /**
     * @return saved trade with each European nation, required; empty until the
     *         first expedition or purchase
     */
    public List<NativeTradePo> getTrade() {
        return trade;
    }

    public void setTrade(final List<NativeTradePo> trade) {
        this.trade = trade;
    }

    /**
     * @return goods bought from carriers, or null when none was saved
     */
    public List<NativeGoodsStockPo> getReceivedGoods() {
        return receivedGoods;
    }

    public void setReceivedGoods(final List<NativeGoodsStockPo> receivedGoods) {
        this.receivedGoods = receivedGoods;
    }

}

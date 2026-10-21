package org.microcol.model.store;

import java.util.List;

import org.microcol.model.MilitaryRole;
import org.microcol.model.MovementPoints;
import org.microcol.model.TradeRoutePause;
import org.microcol.model.UnitType;
import org.microcol.model.Direction;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;

/**
 * Persistent object for unit. All unit hierarchy is stored into this object
 * with discriminator {@link UnitType}.
 */
public class UnitPo {

    private Integer id;

    private UnitType type;

    private String ownerId;

    private PlaceMapPo placeMap;

    private PlaceEuropePortPo placeEuropePort;

    private PlaceHighSeasPo placeHighSeas;

    private PlaceShipRepairPo placeShipRepair;

    public PlaceShipRepairPo getPlaceShipRepair() {
        return placeShipRepair;
    }

    public void setPlaceShipRepair(final PlaceShipRepairPo value) {
        placeShipRepair = value;
    }

    private PlaceConstructionSlotPo placeConstructionSlot;

    private PlaceColonyFieldPo placeColonyField;

    private PlaceCargoSlotPo placeCargoSlot;

    private PlaceNativeCoursePo placeNativeCourse;

    private CargoPo cargo = new CargoPo();

    private UnitActionPo action;

    /**
     * Remaining movement in thirds of a point. It is boxed so that a missing
     * value is rejected on load instead of becoming zero.
     */
    private Integer movementThirds;

    private int tools;

    private boolean holdingGuns;

    private boolean mounted;

    private Integer workUpdates;

    private UnitType workSkill;

    public Integer getWorkUpdates() {
        return workUpdates;
    }

    public void setWorkUpdates(final Integer value) {
        workUpdates = value;
    }

    public UnitType getWorkSkill() {
        return workSkill;
    }

    public void setWorkSkill(final UnitType value) {
        workSkill = value;
    }

    private boolean veteran;

    public boolean isVeteran() {
        return veteran;
    }

    public void setVeteran(final boolean veteran) {
        this.veteran = veteran;
    }

    /**
     * Combat role; an absent value means the ordinary colonial role and an
     * explicit null is rejected on load.
     */
    private MilitaryRole militaryRole = MilitaryRole.COLONIAL;

    public MilitaryRole getMilitaryRole() {
        return militaryRole;
    }

    public void setMilitaryRole(final MilitaryRole militaryRole) {
        this.militaryRole = militaryRole;
    }

    /**
     * Missionary role of a free colonist. It is boxed and required for every
     * colonist, so a missing value is rejected on load instead of becoming
     * ordinary.
     */
    private Boolean missionary;

    public Boolean getMissionary() {
        return missionary;
    }

    public void setMissionary(final Boolean value) {
        missionary = value;
    }

    private Boolean artilleryDamaged;

    /**
     * Home settlement of a native brave. Required only for {@code BRAVE}; it is
     * absent when {@link #nativeHomeless} is true.
     */
    private Integer nativeHomeSettlementId;

    /** Explicit homeless flag of a native brave; required only for braves. */
    private Boolean nativeHomeless;

    /** Combat role name of a native brave; required only for braves. */
    private String nativeRole;

    public Integer getNativeHomeSettlementId() {
        return nativeHomeSettlementId;
    }

    public void setNativeHomeSettlementId(
            final Integer nativeHomeSettlementId) {
        this.nativeHomeSettlementId = nativeHomeSettlementId;
    }

    public Boolean getNativeHomeless() {
        return nativeHomeless;
    }

    public void setNativeHomeless(final Boolean nativeHomeless) {
        this.nativeHomeless = nativeHomeless;
    }

    public String getNativeRole() {
        return nativeRole;
    }

    public void setNativeRole(final String nativeRole) {
        this.nativeRole = nativeRole;
    }

    public Boolean getArtilleryDamaged() {
        return artilleryDamaged;
    }

    public void setArtilleryDamaged(final Boolean artilleryDamaged) {
        this.artilleryDamaged = artilleryDamaged;
    }

    private Location departureLocation;

    private Direction departureDirection;

    public Location getDepartureLocation() {
        return departureLocation;
    }

    public void setDepartureLocation(final Location departureLocation) {
        this.departureLocation = departureLocation;
    }

    public Direction getDepartureDirection() {
        return departureDirection;
    }

    public void setDepartureDirection(final Direction departureDirection) {
        this.departureDirection = departureDirection;
    }

    /**
     * Waiting immigrant on the European pier. An absent value means an ordinary
     * unit, so no migration is needed.
     */
    private boolean immigrant;

    /** Opt-out of a waiting immigrant; only meaningful with immigrant. */
    private boolean boardingOptOut;

    /** Front-of-queue choice; only meaningful for a willing immigrant. */
    private boolean boardingPriority;

    public boolean isImmigrant() {
        return immigrant;
    }

    public void setImmigrant(final boolean immigrant) {
        this.immigrant = immigrant;
    }

    public boolean isBoardingOptOut() {
        return boardingOptOut;
    }

    public void setBoardingOptOut(final boolean boardingOptOut) {
        this.boardingOptOut = boardingOptOut;
    }

    public boolean isBoardingPriority() {
        return boardingPriority;
    }

    public void setBoardingPriority(final boolean boardingPriority) {
        this.boardingPriority = boardingPriority;
    }

    /** Destination of a go-to order; absent means the unit has no order. */
    private Location goToDestination;

    private Integer goToColonyId;

    private String goToDestinationName;

    private boolean goToPaused;

    public Location getGoToDestination() {
        return goToDestination;
    }

    public void setGoToDestination(final Location goToDestination) {
        this.goToDestination = goToDestination;
    }

    public Integer getGoToColonyId() {
        return goToColonyId;
    }

    public void setGoToColonyId(final Integer goToColonyId) {
        this.goToColonyId = goToColonyId;
    }

    public String getGoToDestinationName() {
        return goToDestinationName;
    }

    public void setGoToDestinationName(final String goToDestinationName) {
        this.goToDestinationName = goToDestinationName;
    }

    public boolean isGoToPaused() {
        return goToPaused;
    }

    public void setGoToPaused(final boolean goToPaused) {
        this.goToPaused = goToPaused;
    }

    /**
     * Route and stop index of the stop this carrier already processed; both
     * absent when no stop is processed.
     */
    private Integer tradeRouteVisitRouteId;

    private Integer tradeRouteVisitStopIndex;

    /** Map square of the processed stop; absent for a stop in Europe. */
    private Location tradeRouteVisitLocation;

    public Location getTradeRouteVisitLocation() {
        return tradeRouteVisitLocation;
    }

    public void setTradeRouteVisitLocation(
            final Location tradeRouteVisitLocation) {
        this.tradeRouteVisitLocation = tradeRouteVisitLocation;
    }

    public Integer getTradeRouteVisitRouteId() {
        return tradeRouteVisitRouteId;
    }

    public void setTradeRouteVisitRouteId(
            final Integer tradeRouteVisitRouteId) {
        this.tradeRouteVisitRouteId = tradeRouteVisitRouteId;
    }

    public Integer getTradeRouteVisitStopIndex() {
        return tradeRouteVisitStopIndex;
    }

    public void setTradeRouteVisitStopIndex(
            final Integer tradeRouteVisitStopIndex) {
        this.tradeRouteVisitStopIndex = tradeRouteVisitStopIndex;
    }

    /**
     * Stable route ID and current stop index of a trade route assignment; both
     * absent when the carrier has no assignment.
     */
    private Integer tradeRouteAssignmentRouteId;

    private Integer tradeRouteAssignmentStopIndex;

    /** Reason the assigned carrier waits; absent while it may continue. */
    private TradeRoutePause tradeRouteAssignmentPause;

    /**
     * Route stops as the assigned carrier accepted them at its last boundary;
     * required whenever the route ID is present.
     */
    private List<TradeRouteStopPo> tradeRouteAssignmentStops;

    public Integer getTradeRouteAssignmentRouteId() {
        return tradeRouteAssignmentRouteId;
    }

    public void setTradeRouteAssignmentRouteId(
            final Integer tradeRouteAssignmentRouteId) {
        this.tradeRouteAssignmentRouteId = tradeRouteAssignmentRouteId;
    }

    public Integer getTradeRouteAssignmentStopIndex() {
        return tradeRouteAssignmentStopIndex;
    }

    public void setTradeRouteAssignmentStopIndex(
            final Integer tradeRouteAssignmentStopIndex) {
        this.tradeRouteAssignmentStopIndex = tradeRouteAssignmentStopIndex;
    }

    public TradeRoutePause getTradeRouteAssignmentPause() {
        return tradeRouteAssignmentPause;
    }

    public void setTradeRouteAssignmentPause(
            final TradeRoutePause tradeRouteAssignmentPause) {
        this.tradeRouteAssignmentPause = tradeRouteAssignmentPause;
    }

    public List<TradeRouteStopPo> getTradeRouteAssignmentStops() {
        return tradeRouteAssignmentStops;
    }

    public void setTradeRouteAssignmentStops(
            final List<TradeRouteStopPo> tradeRouteAssignmentStops) {
        this.tradeRouteAssignmentStops = tradeRouteAssignmentStops;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(UnitPo.class).add("id", id)
                .add("type", type).add("ownerId", ownerId)
                .add("movementThirds", movementThirds).add("placeMap", placeMap)
                .add("placeEuropePort", placeEuropePort)
                .add("placeHighSeas", placeHighSeas)
                .add("placeCargoSlot", placeCargoSlot)
                .add("placeConstructionSlot", placeConstructionSlot)
                .add("placeColonyField", placeColonyField).add("cargo", cargo)
                .add("action", action).toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UnitType getType() {
        return type;
    }

    public void setType(UnitType type) {
        this.type = type;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public CargoPo getCargo() {
        return cargo;
    }

    public void setCargo(CargoPo cargo) {
        this.cargo = cargo;
    }

    public Integer getMovementThirds() {
        return movementThirds;
    }

    public void setMovementThirds(final Integer movementThirds) {
        this.movementThirds = movementThirds;
    }

    /**
     * Whole points left, for code that counts points. The saved value stays in
     * thirds.
     *
     * @return whole movement points
     */
    public int getAvailableMoves() {
        return movementThirds / MovementPoints.THIRDS_PER_POINT;
    }

    /**
     * Sets whole movement points, stored as thirds.
     *
     * @param availableMoves required whole movement points
     */
    public void setAvailableMoves(final int availableMoves) {
        movementThirds = MovementPoints.ofPoints(availableMoves);
    }

    public PlaceMapPo getPlaceMap() {
        return placeMap;
    }

    public void setPlaceMap(PlaceMapPo placeMap) {
        this.placeMap = placeMap;
    }

    public PlaceEuropePortPo getPlaceEuropePort() {
        return placeEuropePort;
    }

    public void setPlaceEuropePort(PlaceEuropePortPo placeEuropePort) {
        this.placeEuropePort = placeEuropePort;
    }

    public PlaceHighSeasPo getPlaceHighSeas() {
        return placeHighSeas;
    }

    public void setPlaceHighSeas(PlaceHighSeasPo placeHighSeas) {
        this.placeHighSeas = placeHighSeas;
    }

    /**
     * @return the placeConstructionSlotPo
     */
    public PlaceConstructionSlotPo getPlaceConstructionSlot() {
        return placeConstructionSlot;
    }

    /**
     * @param placeConstructionSlotPo the placeConstructionSlotPo to set
     */
    public void setPlaceConstructionSlot(
            PlaceConstructionSlotPo placeConstructionSlotPo) {
        this.placeConstructionSlot = placeConstructionSlotPo;
    }

    /**
     * @return the saved native course place, or null when not learning
     */
    public PlaceNativeCoursePo getPlaceNativeCourse() {
        return placeNativeCourse;
    }

    /**
     * @param placeNativeCourse the native course place to set, or null
     */
    public void setPlaceNativeCourse(
            final PlaceNativeCoursePo placeNativeCourse) {
        this.placeNativeCourse = placeNativeCourse;
    }

    /**
     * @return the placeColonyFieldPo
     */
    public PlaceColonyFieldPo getPlaceColonyField() {
        return placeColonyField;
    }

    /**
     * @param placeColonyFieldPo the placeColonyFieldPo to set
     */
    public void setPlaceColonyField(PlaceColonyFieldPo placeColonyFieldPo) {
        this.placeColonyField = placeColonyFieldPo;
    }

    /**
     * @return the placeCargoSlot
     */
    public PlaceCargoSlotPo getPlaceCargoSlot() {
        return placeCargoSlot;
    }

    /**
     * @param placeCargoSlot the placeCargoSlot to set
     */
    public void setPlaceCargoSlot(PlaceCargoSlotPo placeCargoSlot) {
        this.placeCargoSlot = placeCargoSlot;
    }

    public UnitActionPo getAction() {
        return action;
    }

    public void setAction(UnitActionPo action) {
        this.action = action;
    }

    /**
     * @return the tools
     */
    public int getTools() {
        return tools;
    }

    /**
     * @param tools the tools to set
     */
    public void setTools(int tools) {
        this.tools = tools;
    }

    /**
     * @return the holdingGuns
     */
    public boolean isHoldingGuns() {
        return holdingGuns;
    }

    /**
     * @param holdingGuns the holdingGuns to set
     */
    public void setHoldingGuns(boolean holdingGuns) {
        this.holdingGuns = holdingGuns;
    }

    /**
     * @return the mounted
     */
    public boolean isMounted() {
        return mounted;
    }

    /**
     * @param mounted the mounted to set
     */
    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

}

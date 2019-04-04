package org.microcol.model.store;

import org.microcol.model.UnitType;

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

    private PlaceConstructionSlotPo placeConstructionSlot;

    private PlaceColonyFieldPo placeColonyField;

    private PlaceCargoSlotPo placeCargoSlot;

    private CargoPo cargo = new CargoPo();

    private UnitActionPo action;

    private int availableMoves;

    private int tools;

    private boolean holdingGuns;

    private boolean mounted;

    public static UnitPo make(final Integer id, final UnitType type, final String ownerId,
            final CargoPo cargo) {
        final UnitPo out = new UnitPo();
        out.setId(id);
        out.setType(type);
        out.setOwnerId(ownerId);
        out.setCargo(cargo);
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(UnitPo.class).add("id", id).add("type", type)
                .add("ownerId", ownerId).add("availableMoves", availableMoves)
                .add("placeMap", placeMap).add("placeEuropePort", placeEuropePort)
                .add("placeHighSeas", placeHighSeas).add("placeCargoSlot", placeCargoSlot)
                .add("placeConstructionSlot", placeConstructionSlot)
                .add("placeColonyField", placeColonyField).add("cargo", cargo).add("action", action)
                .toString();
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

    public int getAvailableMoves() {
        return availableMoves;
    }

    public void setAvailableMoves(int availableMoves) {
        this.availableMoves = availableMoves;
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
     * @param placeConstructionSlotPo
     *            the placeConstructionSlotPo to set
     */
    public void setPlaceConstructionSlot(PlaceConstructionSlotPo placeConstructionSlotPo) {
        this.placeConstructionSlot = placeConstructionSlotPo;
    }

    /**
     * @return the placeColonyFieldPo
     */
    public PlaceColonyFieldPo getPlaceColonyField() {
        return placeColonyField;
    }

    /**
     * @param placeColonyFieldPo
     *            the placeColonyFieldPo to set
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
     * @param placeCargoSlot
     *            the placeCargoSlot to set
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
     * @param tools
     *            the tools to set
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
     * @param holdingGuns
     *            the holdingGuns to set
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
     * @param mounted
     *            the mounted to set
     */
    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

}

package org.microcol.model.store;

import org.microcol.model.UnitType;

import com.google.common.base.MoreObjects;

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

}

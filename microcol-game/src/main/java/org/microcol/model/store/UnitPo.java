package org.microcol.model.store;

public class UnitPo {

	private Integer id;

	private String type;

	private String ownerId;

	private PlaceMapPo placeMap;
	
	private PlaceEuropePortPo placeEuropePort;
	
	private PlaceHighSeasPo placeHighSeas;

	private CargoPo cargo;

	private int availableMoves;
	
	public UnitPo() {
		// TODO Auto-generated constructor stub
	}

	public static UnitPo make(final Integer id, final String type, final String ownerId, final CargoPo cargo) {
		final UnitPo out = new UnitPo();
		out.setId(id);
		out.setType(type);
		out.setOwnerId(ownerId);
		out.setCargo(cargo);
		return out;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
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

}

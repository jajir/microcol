package org.microcol.model.store;

import java.util.HashMap;
import java.util.Map;

public class PlayerPo {

	private String name;
	
	private boolean computer;
	
	private int gold;
	
	private boolean declaredIndependence;
	
	private String whosKingThisPlayerIs;
	
	private Map<String, Object> extraData = new HashMap<>();

	private VisibilityPo visible;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
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
	 * @param computer
	 *            the computer to set
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
	 * @param gold
	 *            the gold to set
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

}

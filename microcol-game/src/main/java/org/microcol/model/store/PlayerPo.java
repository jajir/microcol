package org.microcol.model.store;

public class PlayerPo {

	private String name;
	
	private boolean computer;
	
	private int gold;

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

}

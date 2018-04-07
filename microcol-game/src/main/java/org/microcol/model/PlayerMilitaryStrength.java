package org.microcol.model;

public class PlayerMilitaryStrength {

    private int strength = 0;

    public int getMilitaryStrength() {
        return strength;
    }

    void addUnitData(final Unit unit) {
        strength += unit.getMilitaryStrenght();
    }
}

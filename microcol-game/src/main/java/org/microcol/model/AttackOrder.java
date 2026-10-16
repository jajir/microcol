package org.microcol.model;

import java.util.Optional;

/**
 * A proposed battle bound to the participants and state shown for confirmation.
 */
public final class AttackOrder {
    private final AbstractUnit attacker;
    private final Unit defender;
    private final Player attackingPlayer;
    private final Player defendingPlayer;
    private final Location origin;
    private final Location target;
    private final boolean declarationRequired;
    private final Optional<CombatStrengths> combatStrengths;

    AttackOrder(final AbstractUnit attacker, final Unit defender) {
        this.attacker = attacker;
        this.defender = defender;
        attackingPlayer = attacker.getOwner();
        defendingPlayer = defender.getOwner();
        origin = attacker.getLocation();
        target = defender.getLocation();
        declarationRequired = !attacker.model.areHostile(attackingPlayer,
                defendingPlayer);
        combatStrengths = CombatStrengths.forBattle(attacker.model, attacker,
                defender);
    }

    public Unit getAttacker() {
        return attacker;
    }

    public Unit getDefender() {
        return defender;
    }

    public Model getModel() {
        return attacker.model;
    }

    public boolean isDeclarationRequired() {
        return declarationRequired;
    }

    public Optional<CombatStrengths> getCombatStrengths() {
        return combatStrengths;
    }

    Location getTarget() {
        return target;
    }

    boolean matchesParticipants() {
        return attacker.getOwner() == attackingPlayer
                && defender.getOwner() == defendingPlayer
                && attacker.isAtPlaceLocation() && defender.isAtPlaceLocation()
                && origin.equals(attacker.getLocation())
                && target.equals(defender.getLocation());
    }

    /**
     * Revalidates the proposal before changing diplomacy, movement or units.
     */
    public void execute() {
        attacker.executeAttack(this);
    }
}
